package ca.uhn.fhir.jpa.starter.interceptors;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Procedure;
import org.hl7.fhir.r4.model.Consent.ProvisionComponent;

import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;

import org.hl7.fhir.r4.model.DomainResource;

public class PolicyEvaluator {
    public final static int EVALUATION_SCOPE_ORGANIZATION = 0x01;
    public final static int EVALUATION_SCOPE_PATIENT = 0x02;

    private int evaluationScope;
    private String patientId;
    private List<Consent> consents;

    public PolicyEvaluator(int evaluationScope, String patientId, List<Consent> consents) {
        this.evaluationScope = evaluationScope;
        this.patientId = patientId;
        this.consents = consents;
    }

    public PolicyEvaluator(int evaluationScope, List<Consent> consents) {
        this(evaluationScope, null, consents);
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public ConsentOutcome evaluate(IBaseResource theResource, ActionEnum action, List<String> issues) {
        Consent.ConsentProvisionType result = Consent.ConsentProvisionType.NULL;
        ArrayList<String> tmpIssues = new ArrayList<String>();
        if (consents != null && consents.size() > 0) {
            for (Consent consent : consents) {
                if (!isConsentValidInContext(consent, theResource))
                    continue;

                Consent.ConsentProvisionType decision = checkProvision(consent.getProvision(), theResource, action, 0, tmpIssues);

                // Consent yang tidak punya opini (NULL) diabaikan, bukan dipakai sebagai
                // operand AND (kalau tidak, NULL akan selalu "membatalkan" DENY/PERMIT
                // dari consent lain karena provisionOperationAnd() memperlakukan NULL
                // sebagai annihilator, bukan identity).
                if (decision == Consent.ConsentProvisionType.NULL)
                    continue;

                result = (result == Consent.ConsentProvisionType.NULL)
                        ? decision
                        : provisionOperationAnd(result, decision);

                // karena menggunakan operation AND maka, 
                // jika sudah jelas DENY maka tidak perlu lanjut cek Consent berikutnya
                if (result == Consent.ConsentProvisionType.DENY)
                    break;
            }
        }

        if (result == Consent.ConsentProvisionType.NULL)
            return ConsentOutcome.PROCEED;
        else if (result == Consent.ConsentProvisionType.DENY) {
            issues.addAll(tmpIssues);
            return ConsentOutcome.REJECT;
        }

        return ConsentOutcome.PROCEED;
    }

    private boolean isConsentValidInContext(Consent consent, IBaseResource theResource) {
        if (consent.hasPatient()) {
            // masih fokus di level organisasi
            if ((evaluationScope & EVALUATION_SCOPE_PATIENT) != EVALUATION_SCOPE_PATIENT)
                return false;
            else {
                // Pastikan consent yang memiliki id pasien sesuai dengan id pasien yang merequest
                if (consent.hasPatient() && consent.getPatient().hasReference() && patientId != null) {
                    if (!consent.getPatient().getReference().equals("Patient/" + patientId))
                        return false;
                }
            }
        }

        if (!consent.hasProvision())
            return false;
        
        // Check consent status
        if (!consent.hasStatus() || 
            consent.getStatus() != Consent.ConsentState.ACTIVE) {
            return false;
        }
        
        return true;
    }

    private Consent.ConsentProvisionType checkProvision(ProvisionComponent provision, IBaseResource theResource, ActionEnum action, int level, List<String> issues) {
        Consent.ConsentProvisionType type = provision.getType();

        // Exclude dari proces pengecekan provision
        if (!isMatchProvisionRequirement(provision, theResource, action))
            return Consent.ConsentProvisionType.NULL;

        Consent.ConsentProvisionType secLabelProvision = checkSecurityLabelProvision(provision, theResource, level, issues);
        if (secLabelProvision != Consent.ConsentProvisionType.NULL)
            return secLabelProvision;

        // cek sub-provision jika ada
        if (provision.hasProvision()) {
            Consent.ConsentProvisionType subResult = Consent.ConsentProvisionType.NULL;

            ArrayList<String> subIssues = new ArrayList<String>();
            for (ProvisionComponent subProvision : provision.getProvision()) {
                Consent.ConsentProvisionType res = checkProvision(subProvision, theResource, action, level + 1, subIssues);
                subResult = provisionOperationOr(subResult, res);
            }

            if (subResult == Consent.ConsentProvisionType.DENY) {
                issues.addAll(subIssues);
            }
            // AND-kan subResult dengan parent type
            type = provisionOperationAnd(type, subResult);
        }

        return type;
    }

    private boolean isMatchProvisionRequirement(ProvisionComponent provision, IBaseResource theResource, ActionEnum action) {
        // provision.getActor() tidak perlu diperiksa, 
        // karena di awal list consent sudah difilter berdasarkan actor sesuai organization ID

        if (!provision.hasType())
            return false;
        
        if (!isProvisionHasValidPeriodInContext(provision, theResource))
            return false;

        if (!isProvisionHasValidActionInContext(provision, action))
            return false;

        if (!isProvisionHasValidContentClassInContext(provision, theResource))
            return false;
        
        if (!isProvisionHasValidContentCodeInContext(provision, theResource))
            return false;

        // Additional context checks can be added here
        // e.g., purpose of use, user role, etc.

        return true;
    }

    private boolean isProvisionHasValidPeriodInContext(ProvisionComponent provision, IBaseResource theResource) {
        if (provision.hasPeriod()) {
            Period period = provision.getPeriod();
            Date now = new Date();
            if (period.hasStart() && now.before(period.getStart())) {
                return false;
            }
            if (period.hasEnd() && now.after(period.getEnd())) {
                return false;
            }
        }

        return true;
    }

    private boolean isProvisionHasValidActionInContext(ProvisionComponent provision, ActionEnum action) {
        if (!provision.hasAction())
            return false;

        boolean actionOk = false;
        for (CodeableConcept code : provision.getAction()) {
            if (!code.hasCoding())
                continue;
            for (Coding coding: code.getCoding()) {
                if (action.getConsentAction().equals(coding.getCode())) {
                    actionOk = true;
                    break;
                }
            }

            if (actionOk)
                break;
        }
        if (!actionOk)
            return false;

        return true;
    }

    private boolean isProvisionHasValidContentClassInContext(ProvisionComponent provision, IBaseResource theResource) {
        if (provision.hasClass_()) {
            boolean wantResourceType = false;
            boolean matchResourceType = false;
            for (Coding coding : provision.getClass_()) {
                if (!coding.hasSystem() || !coding.hasCode())
                    continue;
                if (coding.getSystem().equals("http://hl7.org/fhir/resource-types")) {
                    wantResourceType = true;
                    if (coding.getCode().equals(theResource.fhirType())) {
                        matchResourceType = true;
                        break;
                    }
                }
            }

            if (wantResourceType && !matchResourceType)
                return false;
        }

        return true;
    }

    private boolean isProvisionHasValidContentCodeInContext(ProvisionComponent provision, IBaseResource theResource) {
        if (provision.hasCode()) {
            boolean codingOk = false;
            for (CodeableConcept code : provision.getCode()) {
                if (!code.hasCoding())
                    continue;

                for (Coding coding : code.getCoding()) {
                    if (theResource instanceof Observation) {
                        Observation observation = (Observation) theResource;

                        if (!observation.hasCode())
                            continue;

                        for (Coding codingObs : observation.getCode().getCoding()) {
                            if (coding.getCode().equals(codingObs.getCode())) {
                                codingOk = true;
                                break;
                            }
                        }
                    } else if (theResource instanceof Procedure) {
                        Procedure procedure = (Procedure) theResource;
                        
                        if (!procedure.hasCode())
                            continue;

                        for (Coding codingObs : procedure.getCode().getCoding()) {
                            if (coding.getCode().equals(codingObs.getCode())) {
                                codingOk = true;
                                break;
                            }
                        }
                    }

                    if (codingOk)
                        break;
                }
            }

            if (!codingOk)
                return false;
        }

        return true;
    }

    private Consent.ConsentProvisionType checkSecurityLabelProvision(ProvisionComponent provision, IBaseResource theResource, int level, List<String> issues) {
        if (provision.hasSecurityLabel()) {
            int evaluationScope = this.evaluationScope & EVALUATION_SCOPE_ORGANIZATION;
            Consent.ConsentProvisionType type = provision.getType();
            DomainResource targetResource = (DomainResource) theResource; // Cast to R4 DomainResource to access Meta methods
            boolean provisionOk = false;
            List<Coding> nonOrgCodings = new ArrayList<Coding>();

            if (!targetResource.hasMeta() || !targetResource.getMeta().hasSecurity()) {
                Consent.ConsentProvisionType negType = provisionOperationNeg(type);
                if (negType == Consent.ConsentProvisionType.DENY) {
                    issues.add("Resource " + theResource.fhirType() + " tidak memiliki Security Label");
                }

                return negType;
            }

            // Hanya berlaku di provision level-0
            if (evaluationScope == EVALUATION_SCOPE_ORGANIZATION) {
                for (Coding codingProvision : provision.getSecurityLabel()) {
                    if (!codingProvision.hasCode() || !codingProvision.hasSystem())
                        continue;

                    if (codingProvision.getSystem().equals(Constants.ORGANIZATION_SECURITY_SYSTEM_NAME)) {
                        // jika muncul security label organization di sub-provision maka, abaikan saja
                        if (level > 0)
                            continue;

                        for (Coding codingResource : targetResource.getMeta().getSecurity()) {
                            if (codingResource.getSystem().equals(Constants.ORGANIZATION_SECURITY_SYSTEM_NAME)
                                && codingResource.hasCode()) {
                                if (codingProvision.getCode().equals(codingResource.getCode())) {
                                    provisionOk = true;
                                    break;
                                }
                            }
                        }

                        if (provisionOk)
                            break;
                    }else {
                        nonOrgCodings.add(codingProvision);
                    }
                }

                if (!provisionOk) {
                    Consent.ConsentProvisionType negType = provisionOperationNeg(type);
                    if (negType == Consent.ConsentProvisionType.DENY) {
                        issues.add("Resource " + theResource.fhirType() + " tidak dapat diakses oleh ID Organisasi anda");
                    }

                    return negType;
                }
            }else {
                nonOrgCodings = provision.getSecurityLabel();
            }

            if (!nonOrgCodings.isEmpty()) {
                provisionOk = false;
                for (Coding codingProvision : nonOrgCodings) {
                    for (Coding codingResource : targetResource.getMeta().getSecurity()) {
                        if (!codingProvision.getSystem().equals(codingResource.getSystem()))
                            continue;

                        if (codingProvision.getSystem().equals("http://terminology.hl7.org/CodeSystem/v3-Confidentiality")) {
                            if (isSecurityLabelAllowed(codingProvision.getCode(), codingResource.getCode())) {
                                provisionOk = true;
                                break;
                            }
                        } else if (codingProvision.getCode().equals(codingResource.getCode())) {
                            provisionOk = true;
                            break;
                        }
                    }

                    if (provisionOk)
                        break;
                }

                if (!provisionOk) {
                    Consent.ConsentProvisionType negType = provisionOperationNeg(type);
                    if (negType == Consent.ConsentProvisionType.DENY) {
                        issues.add("Security Label di resource " + theResource.fhirType() + " tidak memenuhi persyaratan dari consent yang ditetapkan");
                    }
                    return negType;
                }
            }
            // empty setelah semua security-label organization di-exclude, 
            // maka dianggap sama dengan tidak punya security label
            else if (level > 0)
                return Consent.ConsentProvisionType.NULL;
            
            return type;
        }

        return Consent.ConsentProvisionType.NULL;
    }

    private boolean isSecurityLabelAllowed(String consentSecLabel, String resourceSecLabel) {
        return securityLabelToInt(consentSecLabel) >= securityLabelToInt(resourceSecLabel);
    }

    private int securityLabelToInt(String secLabel) {
        switch (secLabel) {
            case "V":
                return 4;
            case "R":
                return 3;
            case "N":
                return 2;
            case "L":
            default:
                return 1;
        }
    }

    public static Consent.ConsentProvisionType provisionOperationNeg(Consent.ConsentProvisionType provision) {
        if (provision == Consent.ConsentProvisionType.PERMIT)
            return Consent.ConsentProvisionType.DENY;
        else if (provision == Consent.ConsentProvisionType.DENY)
            return Consent.ConsentProvisionType.PERMIT;

        return Consent.ConsentProvisionType.NULL;
    }

    public static Consent.ConsentProvisionType provisionOperationOr(Consent.ConsentProvisionType provision1, Consent.ConsentProvisionType provision2) {
        if (provision1 == Consent.ConsentProvisionType.NULL)
            return provision2;
        else if (provision2 == Consent.ConsentProvisionType.NULL)
            return provision1;
        else if (provision1 == Consent.ConsentProvisionType.PERMIT || provision2 == Consent.ConsentProvisionType.PERMIT)
            return Consent.ConsentProvisionType.PERMIT;
        else if (provision1 == Consent.ConsentProvisionType.NULL && provision2 == Consent.ConsentProvisionType.NULL)
            return Consent.ConsentProvisionType.NULL;

        return Consent.ConsentProvisionType.DENY;
    }

    public static Consent.ConsentProvisionType provisionOperationAnd(Consent.ConsentProvisionType provision1, Consent.ConsentProvisionType provision2) {
        if (provision1 == Consent.ConsentProvisionType.NULL || provision2 == Consent.ConsentProvisionType.NULL)
            return Consent.ConsentProvisionType.NULL;
        else if (provision1 == Consent.ConsentProvisionType.PERMIT && provision2 == Consent.ConsentProvisionType.PERMIT)
            return Consent.ConsentProvisionType.PERMIT;

        return Consent.ConsentProvisionType.DENY;
    }
}
