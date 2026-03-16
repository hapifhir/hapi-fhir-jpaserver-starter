package ca.uhn.fhir.jpa.starter.interceptors;

import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CarePlan;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.EpisodeOfCare;
import org.hl7.fhir.r4.model.Goal;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ServiceRequest;
import org.hl7.fhir.r4.model.Specimen;
import org.hl7.fhir.r4.model.Substance;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationConstants;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOperationStatusEnum;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;

@Interceptor(order = AuthorizationConstants.ORDER_AUTH_INTERCEPTOR + 50)
public class ResourceManipulationInterceptor {
    @Autowired
    private DaoRegistry resourceDaoRegistry;

    @Autowired
    private ConsentProperties consentProperties;

    private LoaderWithCache loader;

    private boolean isAdminAccess = false;

    public ResourceManipulationInterceptor() {
        loader = new LoaderWithCache(consentProperties);
        loader.setResourceDaoRegistry(resourceDaoRegistry);
    }

    @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_CREATED)
    public void insertResource(IBaseResource theResource, RequestDetails theRequestDetails) {
        checkIfAdminAccess(theRequestDetails);

        Class<?> resourceClass = theResource.getClass();
        String patientId = Utils.getPatientId(theResource);
        String userOrgId = theRequestDetails.getHeader(consentProperties.getOrganizationRequestHeader());
        boolean skipEvaluation = isAdminAccess;
        if (theResource instanceof Encounter) {
            checkEncounterServiceProvider(theResource, userOrgId);

            addSecurityLabelForOrganization(theResource, userOrgId);

            // Cek apakah theResource.episodeOfCare tersedia
            // Jika ya, maka load resource EpisodeOfCare tersebut dan tambahkan userOrgId sebagai salah satu CareTeam (pastikan tidak duplikat)
            // CareTeam digunakan untuk menentukan apakah Faskes boleh mengakses Encounter dan Resource yang terkait encounter yang memiliki EpisodeOfCare yang sama
            addCareTeamToEpisodeOfCare((Encounter) theResource, userOrgId, theRequestDetails);
        } else if (theResource instanceof EpisodeOfCare) {
            checkManagingOrganizationCreate(theResource, theRequestDetails);
        } else if (consentProperties.getFhirResourceHasEncounter().contains(resourceClass)) {
            String encounterId = Utils.getEncounterId(theResource);
            if (encounterId != null) {
                Encounter encounter = loader.getResource(encounterId, Encounter.class, theRequestDetails);
                if (encounter != null) {
                    copySecurityLabel(encounter, theResource);

                    if (theResource instanceof ServiceRequest) {
                        // Cek apakah theResource.basedOn bertipe ServiceRequest tersedia
                        // Jika ya, cek apakah ServiceRequest.requester berasal dari Organization yang berbeda
                        // Dan jika ya, tambahkan security label organization requester ke theResource
                        handleServiceRequestBasedOnAnotherServiceRequest((ServiceRequest) theResource, theRequestDetails, userOrgId);

                        // Cek apakah theResource.requester berasal dari Organization yang berbeda
                        // Jika ya, tambahkan security label organization requester ke theResource. Pastikan security label tidak duplikat
                        handleServiceRequestRequester((ServiceRequest) theResource, userOrgId);
                    }
                } else {
                    throw new UnprocessableEntityException(theResource.fhirType() + ".encounter resource wajib diisi",
                        createForbiddenOperationOutcome(theResource.fhirType() + ".encounter is required"));
                }
            } else {
                // Jika encounter tidak ditemukan minimal security label Organisasi ditambahkan
                addSecurityLabelForOrganization(theResource, userOrgId);
            }
        } else if (Constants.FHIR_RESOURCE_CONFORMANNCE.contains(resourceClass)
                || Constants.FHIR_RESOURCE_TERMINOLOGY.contains(resourceClass)
                || Constants.FHIR_RESOURCE_CLINICAL_REASONING.contains(resourceClass)) {
            if (!isAdminAccess) {
                throw toForbiddenOperationException("Anda hanya diperbolehkan membuat resource " + theResource.fhirType(),
                    createForbiddenOperationOutcome("You are only allowed to create resource " + theResource.fhirType()));
            }
        } else {
            boolean addSecLabel = false;
            String encounterId = getEncounterIdIndirect(theResource, theRequestDetails);
            if (encounterId != null) {
                Encounter encounter = loader.getResource(encounterId, Encounter.class, theRequestDetails);
                if (encounter != null) {
                    copySecurityLabel(encounter, theResource);
                    addSecLabel = true;
                }
            }

            if (!addSecLabel)
                addSecurityLabelForOrganization(theResource, userOrgId);
        }

        if (!skipEvaluation)
            applyPolicyEvaluation(userOrgId, patientId, ActionEnum.CREATE, theResource);
    }

    @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_UPDATED)
    public void updateResource(IBaseResource theOldResource, IBaseResource theNewResource, RequestDetails theRequestDetails) {
        checkIfAdminAccess(theRequestDetails);

        Class<?> resourceClass = theNewResource.getClass();
        String patientId = Utils.getPatientId(theNewResource);
        String userOrgId = theRequestDetails.getHeader(consentProperties.getOrganizationRequestHeader());
        boolean skipEvaluation = isAdminAccess;
        if (theNewResource instanceof Encounter) {
            checkEncounterServiceProvider(theNewResource, userOrgId);

            Encounter encNew = (Encounter) theNewResource;
            Encounter encOld = (Encounter) theOldResource;

            // update episode of care terlambat
            if (!encOld.hasEpisodeOfCare() && encNew.hasEpisodeOfCare()) {
                addCareTeamToEpisodeOfCare(encNew, userOrgId, theRequestDetails);
            }
        }else if (theNewResource instanceof EpisodeOfCare) {
            // Tolak perubahan ManagingOrganization waktu update
            checkManagingOrganizationUpdate(theOldResource, theNewResource, theRequestDetails);

            // Tolak perubahan EpisodeOfCare.team dari request client, tapi perbolehkan jika penambahan dilakukan secara internal (didalam interceptor ini)
            checkCareTeamModification(theOldResource, theNewResource, theRequestDetails);
        } else if (Constants.FHIR_RESOURCE_CONFORMANNCE.contains(resourceClass)
                || Constants.FHIR_RESOURCE_TERMINOLOGY.contains(resourceClass)
                || Constants.FHIR_RESOURCE_CLINICAL_REASONING.contains(resourceClass)) {
            if (!isAdminAccess) {
                throw toForbiddenOperationException("Anda hanya diperbolehkan mengubah resource " + theNewResource.fhirType(),
                    createForbiddenOperationOutcome("You are only allowed to update resource " + theNewResource.fhirType()));
            }
        }
     
        if (!skipEvaluation)
            applyPolicyEvaluation(userOrgId, patientId, ActionEnum.UPDATE, theNewResource);
    }

    @Hook(Pointcut.STORAGE_PRESTORAGE_RESOURCE_DELETED)
    public void deleteResource(IBaseResource theResource, RequestDetails theRequestDetails) {
        checkIfAdminAccess(theRequestDetails);

        Class<?> resourceClass = theResource.getClass();
        String patientId = Utils.getPatientId(theResource);
        String userOrgId = theRequestDetails.getHeader(consentProperties.getOrganizationRequestHeader());
        boolean skipEvaluation = isAdminAccess;
        if (Constants.FHIR_RESOURCE_CONFORMANNCE.contains(resourceClass)
                || Constants.FHIR_RESOURCE_TERMINOLOGY.contains(resourceClass)
                || Constants.FHIR_RESOURCE_CLINICAL_REASONING.contains(resourceClass)) {
            if (!isAdminAccess) {
                throw toForbiddenOperationException("Anda hanya diperbolehkan menghapus resource " + theResource.fhirType(),
                    createForbiddenOperationOutcome("You are only allowed to delete resource " + theResource.fhirType()));
            }
        }

        if (!skipEvaluation)
            applyPolicyEvaluation(userOrgId, patientId, ActionEnum.DELETE, theResource);
    }

    private void checkIfAdminAccess(RequestDetails theRequestDetails) {
        // DANGER!!!
        // Pastikan frontend service menghapus ADMINISTRATOR_REQUEST_HEADER agar tidak disalahgunakan
        String addmin = theRequestDetails.getHeader(consentProperties.getAdministratorRequestHeader());
        isAdminAccess = "ADMIN".equals(addmin);
    }

    private void checkEncounterServiceProvider(IBaseResource theResource, String userOrgId) {
        Encounter encounter = (Encounter) theResource;
        if (encounter.hasServiceProvider()) {
            String serviceProviderRef = encounter.getServiceProvider().getReference();
            
            // jika berasal dari admin (contoh; karena proses copy dari SATUSEHAT) biarkan saja apa adanya
            if (!isAdminAccess && !serviceProviderRef.equals("Organization/" + userOrgId)) {
                throw toForbiddenOperationException("Anda hanya diperbolehkan membuat Encounter untuk organisasi Anda sendiri.",
                    createForbiddenOperationOutcome("You are only allowed to create Encounter.serviceProvider for your own organization"));
            }
        } else {
            throw new UnprocessableEntityException("Field serviceProvider wajib diisi.", 
                createForbiddenOperationOutcome("Encounter.serviceProvider is required"));
        }
    }

    private void copySecurityLabel(Encounter enc, IBaseResource theResource) {
        if (enc == null || theResource == null) {
            return;
        }

        if (!enc.hasMeta() || !enc.getMeta().hasSecurity()) {
            return;
        }

        DomainResource targetResource = (DomainResource) theResource;
        Meta targetMeta = targetResource.getMeta();
        if (targetMeta == null) {
            targetMeta = new Meta();
            targetResource.setMeta(targetMeta);
        }

        // Copy security labels from Encounter to target resource without duplicates
        for (Coding sourceSecurity : enc.getMeta().getSecurity()) {
            boolean exists = false;

            // Check if this security label already exists in the target resource
            if (targetMeta.hasSecurity()) {
                for (Coding targetSecurity : targetMeta.getSecurity()) {
                    if (isSecurityLabelEqual(sourceSecurity, targetSecurity)) {
                        exists = true;
                        break;
                    }
                }
            }

            // Add only if it doesn't already exist (avoid duplicates)
            if (!exists) {
                Coding newSecurity = sourceSecurity.copy();
                targetMeta.addSecurity(newSecurity);
            }
        }
    }

    /**
     * Add security label for an organization to a resource
     */
    private void addSecurityLabelForOrganization(IBaseResource theResource, String orgId) {
        if (theResource == null || orgId == null) {
            return;
        }

        // Cast to R4 DomainResource to access Meta methods
        DomainResource targetResource = (DomainResource) theResource;

        // Get or create Meta for the target resource
        Meta targetMeta = targetResource.getMeta();
        if (targetMeta == null) {
            targetMeta = new Meta();
            targetResource.setMeta(targetMeta);
        }

        // Create new security label
        Coding newSecurity = new Coding();
        newSecurity.setSystem(consentProperties.getOrganizationSecuritySystemName());
        newSecurity.setCode(orgId);

        // Check if this security label already exists
        boolean exists = false;
        if (targetMeta.hasSecurity()) {
            for (Coding existingSecurity : targetMeta.getSecurity()) {
                if (isSecurityLabelEqual(newSecurity, existingSecurity)) {
                    exists = true;
                    break;
                }
            }
        }

        // Add only if it doesn't already exist (avoid duplicates)
        if (!exists) {
            targetMeta.addSecurity(newSecurity);
        }
    }

    /**
     * Check if two security labels are equal by comparing system and code
     */
    private boolean isSecurityLabelEqual(Coding security1, Coding security2) {
        if (security1 == null || security2 == null) {
            return false;
        }

        boolean systemEqual = (security1.getSystem() == null && security2.getSystem() == null)
                || (security1.getSystem() != null && security1.getSystem().equals(security2.getSystem()));

        boolean codeEqual = (security1.getCode() == null && security2.getCode() == null)
                || (security1.getCode() != null && security1.getCode().equals(security2.getCode()));

        return systemEqual && codeEqual;
    }

    /**
     * Handle ServiceRequest basedOn references - check for requester from different organizations
     * and add security labels accordingly
     */
    private void handleServiceRequestBasedOnAnotherServiceRequest(ServiceRequest serviceRequest, RequestDetails theRequestDetails, String userOrgId) {
        if (serviceRequest == null || !serviceRequest.hasBasedOn()) {
            return;
        }

        // Process each basedOn reference
        for (Reference basedOnRef : serviceRequest.getBasedOn()) {
            if (!basedOnRef.hasReference() || !basedOnRef.getReference().startsWith("ServiceRequest/")) {
                continue;
            }

            // Extract ServiceRequest ID
            String serviceRequestId = basedOnRef.getReference().substring("ServiceRequest/".length());
            if (serviceRequestId == null || serviceRequestId.isEmpty()) {
                continue;
            }

            // Load the referenced ServiceRequest
            ServiceRequest basedOnServiceRequest = loader.getResource(serviceRequestId, ServiceRequest.class, theRequestDetails);
            if (basedOnServiceRequest == null) {
                continue;
            }

            // Check if the basedOn ServiceRequest has a requester from a different organization
            if (basedOnServiceRequest.hasRequester() && basedOnServiceRequest.getRequester().hasReference()) {
                String requesterRef = basedOnServiceRequest.getRequester().getReference();

                // Check if requester is an Organization
                if (requesterRef.startsWith("Organization/")) {
                    String requesterOrgId = requesterRef.substring("Organization/".length());

                    // Check if requester organization is different from encounter's organization
                    // and different from current user's organization
                    if (!requesterOrgId.equals(userOrgId)) {
                        // Add security label for the requester's organization
                        addSecurityLabelForOrganization(serviceRequest, requesterOrgId);
                    }
                }
            }
        }
    }

    /**
     * Handle ServiceRequest requester - check if requester is from a different organization
     * and add security label accordingly
     */
    private void handleServiceRequestRequester(ServiceRequest serviceRequest, String userOrgId) {
        if (serviceRequest == null || !serviceRequest.hasRequester()) {
            return;
        }

        // Get requester reference
        Reference requesterRef = serviceRequest.getRequester();
        if (!requesterRef.hasReference()) {
            return;
        }

        // Check if requester is an Organization
        if (requesterRef.getReference().startsWith("Organization/")) {
            String requesterOrgId = requesterRef.getReference().substring("Organization/".length());

            // Check if requester organization is different from current user's organization
            if (!requesterOrgId.equals(userOrgId)) {
                // Add security label for the requester's organization
                addSecurityLabelForOrganization(serviceRequest, requesterOrgId);
            }
        }
    }

    /**
     * Check if Encounter has episodeOfCare references and add user's organization to CareTeam
     * without duplicates
     */
    private void addCareTeamToEpisodeOfCare(Encounter encounter, String userOrgId, RequestDetails theRequestDetails) {
        if (encounter == null || !encounter.hasEpisodeOfCare()) {
            return;
        }

        // Process each episodeOfCare reference
        for (Reference eocRef : encounter.getEpisodeOfCare()) {
            if (!eocRef.hasReference() || !eocRef.getReference().startsWith("EpisodeOfCare/")) {
                continue;
            }

            // Extract EpisodeOfCare ID
            String eocId = eocRef.getReference().substring("EpisodeOfCare/".length());
            if (eocId == null || eocId.isEmpty()) {
                continue;
            }

            // Load the EpisodeOfCare
            EpisodeOfCare episodeOfCare = loader.getResource(eocId, EpisodeOfCare.class, theRequestDetails);
            if (episodeOfCare == null) {
                continue;
            }

            // Add user's organization to CareTeam without duplicates
            addOrganizationToCareTeam(episodeOfCare, userOrgId, theRequestDetails);
        }
    }

    /**
     * Add organization to CareTeam without duplicates
     */
    private void addOrganizationToCareTeam(EpisodeOfCare episodeOfCare, String userOrgId, RequestDetails theRequestDetails) {
        if (episodeOfCare == null || userOrgId == null) {
            return;
        }

        // Create new CareTeam member reference for the organization
        Reference newParticipant = new Reference("Organization/" + userOrgId);

        // Check if this organization already exists in CareTeam
        boolean exists = false;
        if (episodeOfCare.hasTeam()) {
            for (Reference existingParticipant : episodeOfCare.getTeam()) {
                if (existingParticipant.hasReference()) {
                    String existingRef = existingParticipant.getReference();
                    if (existingRef.equals("Organization/" + userOrgId)) {
                        exists = true;
                        break;
                    }
                }
            }
        }

        // Add only if it doesn't already exist (avoid duplicates)
        if (!exists) {
            // Tandai modifikasi internal
            Map<Object, Object> userData = theRequestDetails.getUserData();
            userData.put("internal-careteam-modification", true);

            episodeOfCare.addTeam(newParticipant);
        }
    }

    private void checkManagingOrganizationCreate(IBaseResource theResource, RequestDetails theRequestDetails) {
        String userOrgId = theRequestDetails.getHeader(consentProperties.getOrganizationRequestHeader());
        EpisodeOfCare eoc = (EpisodeOfCare) theResource;

        if (eoc.hasManagingOrganization()) {
            String serviceProviderRef = eoc.getManagingOrganization().getReference();
            String orgId = serviceProviderRef.substring("Organization/".length());

            // Hanya boleh pakai organization ID sendiri atau KEMENKES untuk  managingOrganization
            if (!orgId.equals(consentProperties.getOrganizationManagingIdKemenkes()) && !orgId.equals(userOrgId)) {
                throw toForbiddenOperationException("Anda hanya diperbolehkan membuat EpisodeOfCare untuk organisasi Anda sendiri.",
                    createForbiddenOperationOutcome("You are only allowed to create EpisodeOfCare for your own organization"));
            }
        } else {
            throw new UnprocessableEntityException("EpisodeOfCare.managingOrganization wajib diisi.",
                    createForbiddenOperationOutcome("EpisodeOfCare.managingOrganization is required"));
        }
    }

    private void checkManagingOrganizationUpdate(IBaseResource theOldResource, IBaseResource theNewResource, RequestDetails theRequestDetails) {
        String userOrgId = theRequestDetails.getHeader(consentProperties.getOrganizationRequestHeader());
        EpisodeOfCare eocNew = (EpisodeOfCare) theNewResource;
        EpisodeOfCare eocOld = (EpisodeOfCare) theOldResource;

        if (!eocNew.hasManagingOrganization() && eocOld.hasManagingOrganization()) {
            throw toForbiddenOperationException("Tidak diperbolehkan menghapus managingOrtanization.",
                    createForbiddenOperationOutcome("Not allowed to delete EpisodeOfCare.managingOrganization"));
        } else if (eocNew.getManagingOrganization().hasReference()
            && eocOld.getManagingOrganization().hasReference()
            && !eocNew.getManagingOrganization().getReference().equals(eocOld.getManagingOrganization().getReference())
            && !eocNew.getManagingOrganization().getReference().equals("Organization/" + userOrgId)) {
            throw toForbiddenOperationException("Tidak diperbolehkan mengganti managingOrtanization ke Faskes lain.",
                    createForbiddenOperationOutcome("Not allowed to change EpisodeOfCare.managingOrganization to another facility"));
        }
    }

    /**
     * Check if EpisodeOfCare.team has been modified by client request
     * Reject client-side changes to team field, but allow internal additions
     */
    private void checkCareTeamModification(IBaseResource theOldResource, IBaseResource theNewResource, RequestDetails theRequestDetails) {
        EpisodeOfCare eocOld = (EpisodeOfCare) theOldResource;
        EpisodeOfCare eocNew = (EpisodeOfCare) theNewResource;

        // Check if team field has been modified
        boolean teamModified = false;
        
        // Check if team list sizes are different
        if ((eocOld.getTeam() == null && eocNew.getTeam() != null) ||
            (eocOld.getTeam() != null && eocNew.getTeam() == null) ||
            (eocOld.getTeam() != null && eocNew.getTeam() != null &&
             eocOld.getTeam().size() != eocNew.getTeam().size())) {
            teamModified = true;
        } else if (eocOld.getTeam() != null && eocNew.getTeam() != null) {
            // Check if team members are different
            for (Reference oldMember : eocOld.getTeam()) {
                boolean found = false;
                for (Reference newMember : eocNew.getTeam()) {
                    if (oldMember.equalsDeep(newMember)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    teamModified = true;
                    break;
                }
            }
        }

        // If team has been modified and it's not an internal operation, reject it
        if (teamModified && !isInternalCareTeamModification(theRequestDetails)) {
            throw toForbiddenOperationException("Tidak diperbolehkan mengubah EpisodeOfCare.team dari request client.",
                    createForbiddenOperationOutcome("Not allowed to modify EpisodeOfCare.team from client request"));
        }
    }

    /**
     * Check if the current operation is an internal CareTeam modification
     * (done by the interceptor, not by client request)
     */
    private boolean isInternalCareTeamModification(RequestDetails theRequestDetails) {
        // Check if there's a flag indicating internal modification
        Map<Object, Object> userData = theRequestDetails.getUserData();
        Object internalFlag = userData.get("internal-careteam-modification");
        return internalFlag != null && Boolean.TRUE.equals(internalFlag);
    }

    public String getEncounterIdIndirect(IBaseResource theResource, RequestDetails theRequestDetails) {
        if (theResource == null) {
            return null;
        }

        // Composition
        if (theResource instanceof Goal) {
            // Load CarePlan.goal yang merefer ke id theResource, lalu panggil return Utils.getEncounterId(careplan)
            return getEncounterIdFromGoal((Goal) theResource, theRequestDetails);
        } else if (theResource instanceof Specimen) {
            // Get ServiceRequest dari Specimen.request lalu panggil return Utils.getEncounterId(serviceRequest)
            return getEncounterIdFromSpecimen((Specimen) theResource, theRequestDetails);
        } else if (theResource instanceof Medication) {
            // Cari dari referensi MedicationRequest.medicationReference atau MedicationDispense.medicationReference 
            // atau MedicationStatement.medicationReference atau MedicationAdministration.medicationReference
            // Berhenti mencari jika pertama ditemukan dan panggil return Utils.getEncounterId(resourceFound)
            return getEncounterIdFromMedication((Medication) theResource, theRequestDetails);
        } else if (theResource instanceof Substance) {
            // Cari dari referensi Specimen.processing.additive lalu panggil return Utils.getEncounterId(specimen)
            // NOTE: FHIR tidak bisa melakukan query secara langsung processing.additive dari specimen.
            //      Untuk sementara karena batasan ini, Substance dimasukkan sebagai WHITELIST
            // return getEncounterIdFromSubstance((Substance) theResource, theRequestDetails);
        }

        return null;
    }

    /**
     * Get Encounter ID from Goal by finding the CarePlan that references this Goal
     */
    private String getEncounterIdFromGoal(Goal goal, RequestDetails theRequestDetails) {
        if (goal == null || !goal.hasIdElement() || !goal.getIdElement().hasIdPart()) {
            return null;
        }

        String goalId = goal.getIdElement().getIdPart();
        
        // Search for CarePlan that has a goal reference to this Goal
        IFhirResourceDao<CarePlan> carePlanDao = (IFhirResourceDao<CarePlan>) resourceDaoRegistry.getResourceDao(CarePlan.class);
        
        try {
            // Create search parameter map for goal reference
            SearchParameterMap paramMap = new SearchParameterMap();
            paramMap.add(CarePlan.SP_GOAL, new ReferenceParam("Goal/" + goalId));
            paramMap.setCount(1);
            
            IBundleProvider bundleProvider = carePlanDao.search(paramMap, theRequestDetails);
            
            if (bundleProvider != null && bundleProvider.size() > 0) {
                // Get the first CarePlan
                List<IBaseResource> resources = bundleProvider.getResources(0, 1);
                if (!resources.isEmpty() && resources.get(0) instanceof CarePlan) {
                    CarePlan carePlan = (CarePlan) resources.get(0);
                    return Utils.getEncounterId(carePlan);
                }
            }
        } catch (Exception e) {}
        
        return null;
    }

    /**
     * Get Encounter ID from Specimen by following the ServiceRequest reference
     */
    private String getEncounterIdFromSpecimen(Specimen specimen, RequestDetails theRequestDetails) {
        if (specimen == null || !specimen.hasRequest()) {
            return null;
        }

        // Get the first ServiceRequest reference from Specimen.request
        for (Reference requestRef : specimen.getRequest()) {
            if (!requestRef.hasReference() || !requestRef.getReference().startsWith("ServiceRequest/")) {
                continue;
            }

            String serviceRequestId = requestRef.getReference().substring("ServiceRequest/".length());
            if (serviceRequestId == null || serviceRequestId.isEmpty()) {
                continue;
            }

            // Load the ServiceRequest
            ServiceRequest serviceRequest = loader.getResource(serviceRequestId, ServiceRequest.class, theRequestDetails);
            if (serviceRequest != null) {
                return Utils.getEncounterId(serviceRequest);
            }
        }

        return null;
    }

    /**
     * Get Encounter ID from Medication by searching for resources that reference this Medication
     */
    private String getEncounterIdFromMedication(Medication medication, RequestDetails theRequestDetails) {
        if (medication == null || !medication.hasIdElement() || !medication.getIdElement().hasIdPart()) {
            return null;
        }

        String medicationId = medication.getIdElement().getIdPart();
        String medicationReference = "Medication/" + medicationId;

        // Search in order: MedicationRequest, MedicationDispense, MedicationStatement, MedicationAdministration
        // Stop when first found
        
        // 1. Try MedicationRequest
        IFhirResourceDao<MedicationRequest> medRequestDao = (IFhirResourceDao<MedicationRequest>) resourceDaoRegistry.getResourceDao(MedicationRequest.class);
        try {
            SearchParameterMap paramMap = new SearchParameterMap();
            paramMap.add(MedicationRequest.SP_MEDICATION, new ReferenceParam(medicationReference));
            paramMap.setCount(1);
            
            IBundleProvider bundleProvider = medRequestDao.search(paramMap, theRequestDetails);
            
            if (bundleProvider != null && bundleProvider.size() > 0) {
                List<IBaseResource> resources = bundleProvider.getResources(0, 1);
                if (!resources.isEmpty() && resources.get(0) instanceof MedicationRequest) {
                    MedicationRequest medRequest = (MedicationRequest) resources.get(0);
                    String encounterId = Utils.getEncounterId(medRequest);
                    if (encounterId != null) {
                        return encounterId;
                    }
                }
            }
        } catch (Exception e) {}

        // 2. Try MedicationDispense
        IFhirResourceDao<MedicationDispense> medDispenseDao = (IFhirResourceDao<MedicationDispense>) resourceDaoRegistry.getResourceDao(MedicationDispense.class);
        try {
            SearchParameterMap paramMap = new SearchParameterMap();
            paramMap.add(MedicationDispense.SP_MEDICATION, new ReferenceParam(medicationReference));
            paramMap.setCount(1);
            
            IBundleProvider bundleProvider = medDispenseDao.search(paramMap, theRequestDetails);
            
            if (bundleProvider != null && bundleProvider.size() > 0) {
                List<IBaseResource> resources = bundleProvider.getResources(0, 1);
                if (!resources.isEmpty() && resources.get(0) instanceof MedicationDispense) {
                    MedicationDispense medDispense = (MedicationDispense) resources.get(0);
                    String encounterId = Utils.getEncounterId(medDispense);
                    if (encounterId != null) {
                        return encounterId;
                    }
                }
            }
        } catch (Exception e) {}

        // SKIP dulu Try 3 dan 4, untuk kurangi beban proses
        // 3. Try MedicationStatement
        /*IFhirResourceDao<MedicationStatement> medStatementDao = (IFhirResourceDao<MedicationStatement>) resourceDaoRegistry.getResourceDao(MedicationStatement.class);
        try {
            SearchParameterMap paramMap = new SearchParameterMap();
            paramMap.add(MedicationStatement.SP_MEDICATION, new ReferenceParam(medicationReference));
            paramMap.setCount(1);
            
            IBundleProvider bundleProvider = medStatementDao.search(paramMap, theRequestDetails);
            
            if (bundleProvider != null && bundleProvider.size() > 0) {
                List<IBaseResource> resources = bundleProvider.getResources(0, 1);
                if (!resources.isEmpty() && resources.get(0) instanceof MedicationStatement) {
                    MedicationStatement medStatement = (MedicationStatement) resources.get(0);
                    String encounterId = Utils.getEncounterId(medStatement);
                    if (encounterId != null) {
                        return encounterId;
                    }
                }
            }
        } catch (Exception e) {}

        // 4. Try MedicationAdministration
        IFhirResourceDao<MedicationAdministration> medAdminDao = (IFhirResourceDao<MedicationAdministration>) resourceDaoRegistry.getResourceDao(MedicationAdministration.class);
        try {
            SearchParameterMap paramMap = new SearchParameterMap();
            paramMap.add(MedicationAdministration.SP_MEDICATION, new ReferenceParam(medicationReference));
            paramMap.setCount(1);
            
            IBundleProvider bundleProvider = medAdminDao.search(paramMap, theRequestDetails);
            
            if (bundleProvider != null && bundleProvider.size() > 0) {
                List<IBaseResource> resources = bundleProvider.getResources(0, 1);
                if (!resources.isEmpty() && resources.get(0) instanceof MedicationAdministration) {
                    MedicationAdministration medAdmin = (MedicationAdministration) resources.get(0);
                    String encounterId = Utils.getEncounterId(medAdmin);
                    if (encounterId != null) {
                        return encounterId;
                    }
                }
            }
        } catch (Exception e) {}*/

        return null;
    }

    private void applyPolicyEvaluation(String userOrgId, String patientId, ActionEnum action, IBaseResource theResource) {
        if (!isAdminAccess) {
            // load daftar consent berdasarkan Organisasi
            PolicyEvaluator policyEvaluator = new PolicyEvaluator(PolicyEvaluator.EVALUATION_SCOPE_ORGANIZATION, patientId, loader.getConsentList(consentProperties.getEnvironment(), userOrgId));
            
            // evaluasi action CREATE
            ArrayList<String> issues = new ArrayList<String>();
            ConsentOutcome outcome = policyEvaluator.evaluate(theResource, action, issues);

            if (outcome.getStatus() == ConsentOperationStatusEnum.REJECT) {
                // Get resource type name
                String resourceType = theResource.fhirType();
                String message;
                switch (action) {
                    case CREATE:
                        message = "You are not allowed to create " + resourceType + " resource based on consent policy.";
                        break;
                    case UPDATE:
                        message = "You are not allowed to update " + resourceType + " resource based on consent policy.";
                        break;
                    case DELETE:
                        message = "You are not allowed to delete " + resourceType + " resource based on consent policy.";
                        break;
                    default:
                        message = "You are not allowed to " + action + " " + resourceType + " resource based on consent policy.";
                        message = "Anda tidak diperbolehkan melakukan aksi ini pada resource " + resourceType + " berdasarkan kebijakan consent.";
                        break;
                }
                
                throw toForbiddenOperationException(message, createForbiddenOperationOutcome(issues));
            }
        }
    }

    private static ForbiddenOperationException toForbiddenOperationException(String message, IBaseOperationOutcome operationOutcome) {
		return new ForbiddenOperationException(message, operationOutcome);
	}

    /**
     * Create an OperationOutcome with issue details for forbidden operations
     */
    private static OperationOutcome createForbiddenOperationOutcome(String diagnostics) {
        OperationOutcome operationOutcome = new OperationOutcome();
        OperationOutcome.OperationOutcomeIssueComponent issue = operationOutcome.addIssue();
        issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
        issue.setCode(OperationOutcome.IssueType.FORBIDDEN);
        issue.setDiagnostics(diagnostics);

        return operationOutcome;
    }

    private static OperationOutcome createForbiddenOperationOutcome(List<String> diagnostics) {
        OperationOutcome operationOutcome = new OperationOutcome();

        for (String diagnostic : diagnostics) {
            OperationOutcome.OperationOutcomeIssueComponent issue = operationOutcome.addIssue();
            issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
            issue.setCode(OperationOutcome.IssueType.FORBIDDEN);
            issue.setDiagnostics(diagnostic);
        }

        return operationOutcome;
    }
}
