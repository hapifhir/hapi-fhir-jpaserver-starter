package ca.uhn.fhir.jpa.starter.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Consent.ConsentProvisionType;
import org.hl7.fhir.r4.model.Consent.ConsentState;
import org.hl7.fhir.r4.model.Consent.ProvisionComponent;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Observation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOperationStatusEnum;
import ca.uhn.fhir.rest.server.interceptor.consent.ConsentOutcome;

/**
 * Unit test murni (tanpa Spring context) untuk {@link PolicyEvaluator}.
 * <p>
 * Ditulis khusus untuk mencegah regresi dari 2 bug yang pernah ditemukan
 * di sesi debugging manual (lihat riwayat perubahan {@code PolicyEvaluator.java}):
 * </p>
 * <ol>
 *   <li>Fold logic {@code evaluate()} yang meng-AND-kan seed {@code NULL} di
 *       iterasi pertama, membuat {@code DENY} tidak pernah ter-propagate.</li>
 *   <li>Security-label mismatch yang di-negasikan (implicit PERMIT) - perilaku
 *       yang sengaja, tapi gampang salah diasumsikan sebagai bug kalau tidak
 *       ada test eksplisit untuk itu.</li>
 * </ol>
 */
class PolicyEvaluatorTest {

    private static final String ORG_ID = "1001";
    private static final String OTHER_ORG_ID = "9999";
    private static final String ORG_SECURITY_SYSTEM = "http://terminology.kemkes.go.id/org-id";
    private static final String ACTION_SYSTEM = "http://terminology.hl7.org/CodeSystem/consentaction";
    private static final String CLASS_SYSTEM = "http://hl7.org/fhir/resource-types";

    // ---------------------------------------------------------------------
    // Helper builders
    // ---------------------------------------------------------------------

    private static Observation observationWithOrgSecurityLabel(String orgId) {
        Observation obs = new Observation();
        if (orgId != null) {
            Meta meta = new Meta();
            meta.addSecurity(new Coding(ORG_SECURITY_SYSTEM, orgId, null));
            obs.setMeta(meta);
        }
        return obs;
    }

    private static ProvisionComponent provision(ConsentProvisionType type, String actionCode, String classCode) {
        ProvisionComponent provision = new ProvisionComponent();
        provision.setType(type);
        if (actionCode != null) {
            provision.addAction(new CodeableConcept().addCoding(new Coding(ACTION_SYSTEM, actionCode, null)));
        }
        if (classCode != null) {
            provision.addClass_(new Coding(CLASS_SYSTEM, classCode, null));
        }
        return provision;
    }

    private static Consent activeConsent(ProvisionComponent provision) {
        Consent consent = new Consent();
        consent.setStatus(ConsentState.ACTIVE);
        consent.setProvision(provision);
        return consent;
    }

    private static PolicyEvaluator evaluator(Consent... consents) {
        return new PolicyEvaluator(PolicyEvaluator.EVALUATION_SCOPE_ORGANIZATION, new ArrayList<>(List.of(consents)));
    }

    // ---------------------------------------------------------------------
    // Kasus dasar
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Kasus dasar (tanpa consent / consent kosong)")
    class BasicCases {

        @Test
        @DisplayName("Tanpa consent sama sekali -> PROCEED (default fail-open)")
        void noConsents_shouldProceed() {
            PolicyEvaluator evaluator = new PolicyEvaluator(PolicyEvaluator.EVALUATION_SCOPE_ORGANIZATION, Collections.emptyList());
            ConsentOutcome outcome = evaluator.evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }

        @Test
        @DisplayName("List consent null -> PROCEED, tidak boleh NullPointerException")
        void nullConsentList_shouldProceed() {
            PolicyEvaluator evaluator = new PolicyEvaluator(PolicyEvaluator.EVALUATION_SCOPE_ORGANIZATION, null);
            ConsentOutcome outcome = evaluator.evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }
    }

    // ---------------------------------------------------------------------
    // REGRESI: fold logic evaluate() - bug utama yang ditemukan sesi ini
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Regresi: fold logic evaluate() (bug utama yang pernah ditemukan)")
    class FoldLogicRegression {

        @Test
        @DisplayName("Satu consent DENY yang match -> HARUS REJECT (bukan PROCEED)")
        void singleMatchingDenyConsent_mustReject() {
            Consent consent = activeConsent(provision(ConsentProvisionType.DENY, "read", "Observation"));
            PolicyEvaluator evaluator = evaluator(consent);

            ConsentOutcome outcome = evaluator.evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            // Sebelum fix: result di-seed NULL lalu di-AND dengan decision pertama,
            // provisionOperationAnd(NULL, DENY) = NULL -> selalu PROCEED (bug).
            assertEquals(ConsentOperationStatusEnum.REJECT, outcome.getStatus(),
                    "Single DENY consent yang match seharusnya REJECT - kalau test ini gagal, fold logic regresi ke bug lama");
        }

        @Test
        @DisplayName("Satu consent PERMIT yang match -> PROCEED")
        void singleMatchingPermitConsent_shouldProceed() {
            Consent consent = activeConsent(provision(ConsentProvisionType.PERMIT, "read", "Observation"));
            PolicyEvaluator evaluator = evaluator(consent);

            ConsentOutcome outcome = evaluator.evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }

        @Test
        @DisplayName("DENY + PERMIT (dua consent match) -> DENY selalu menang (safety-first AND)")
        void denyAndPermitCombined_denyShouldWin() {
            Consent denyConsent = activeConsent(provision(ConsentProvisionType.DENY, "read", "Observation"));
            Consent permitConsent = activeConsent(provision(ConsentProvisionType.PERMIT, "read", "Observation"));

            // urutan 1: DENY dulu baru PERMIT
            ConsentOutcome outcome1 = evaluator(denyConsent, permitConsent)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());
            assertEquals(ConsentOperationStatusEnum.REJECT, outcome1.getStatus());

            // urutan 2: PERMIT dulu baru DENY - hasil harus sama (operasi AND komutatif)
            ConsentOutcome outcome2 = evaluator(permitConsent, denyConsent)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());
            assertEquals(ConsentOperationStatusEnum.REJECT, outcome2.getStatus());
        }

        @Test
        @DisplayName("Dua consent PERMIT -> tetap PROCEED")
        void twoPermitConsents_shouldProceed() {
            Consent permit1 = activeConsent(provision(ConsentProvisionType.PERMIT, "read", "Observation"));
            Consent permit2 = activeConsent(provision(ConsentProvisionType.PERMIT, "read", null));

            ConsentOutcome outcome = evaluator(permit1, permit2)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }
    }

    // ---------------------------------------------------------------------
    // Filter provision: action, class, status
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Filter provision (action, class, status consent)")
    class ProvisionFiltering {

        @Test
        @DisplayName("Consent DENY dengan action berbeda (collect) tidak berlaku utk action READ -> PROCEED")
        void mismatchedAction_shouldNotApply() {
            Consent consent = activeConsent(provision(ConsentProvisionType.DENY, "collect", "Observation"));
            ConsentOutcome outcome = evaluator(consent)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }

        @Test
        @DisplayName("Consent DENY dengan class berbeda (Condition) tidak berlaku utk resource Observation -> PROCEED")
        void mismatchedResourceClass_shouldNotApply() {
            Consent consent = activeConsent(provision(ConsentProvisionType.DENY, "read", "Condition"));
            ConsentOutcome outcome = evaluator(consent)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }

        @Test
        @DisplayName("Consent DENY tapi class match utk resource yang benar-benar Condition -> REJECT")
        void matchingResourceClass_shouldApply() {
            Consent consent = activeConsent(provision(ConsentProvisionType.DENY, "read", "Condition"));
            Condition condition = new Condition();
            Meta meta = new Meta();
            meta.addSecurity(new Coding(ORG_SECURITY_SYSTEM, ORG_ID, null));
            condition.setMeta(meta);

            ConsentOutcome outcome = evaluator(consent).evaluate(condition, ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.REJECT, outcome.getStatus());
        }

        @Test
        @DisplayName("Consent dengan status BUKAN active (mis. draft) -> diabaikan, PROCEED walau provision DENY")
        void inactiveConsent_shouldBeIgnored() {
            Consent consent = activeConsent(provision(ConsentProvisionType.DENY, "read", "Observation"));
            consent.setStatus(ConsentState.DRAFT);

            ConsentOutcome outcome = evaluator(consent)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }

        @Test
        @DisplayName("Consent tanpa provision sama sekali -> diabaikan, PROCEED")
        void consentWithoutProvision_shouldBeIgnored() {
            Consent consent = new Consent();
            consent.setStatus(ConsentState.ACTIVE);
            // sengaja tidak setProvision(...)

            ConsentOutcome outcome = evaluator(consent)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }
    }

    // ---------------------------------------------------------------------
    // Security label (organization-level)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Security label organisasi")
    class SecurityLabelMatching {

        @Test
        @DisplayName("Security-label provision MATCH security-label resource -> DENY diterapkan")
        void matchingSecurityLabel_denyApplies() {
            ProvisionComponent p = provision(ConsentProvisionType.DENY, "read", "Observation");
            p.addSecurityLabel(new Coding(ORG_SECURITY_SYSTEM, ORG_ID, null));
            Consent consent = activeConsent(p);

            ConsentOutcome outcome = evaluator(consent)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.REJECT, outcome.getStatus());
        }

        @Test
        @DisplayName("Security-label provision TIDAK MATCH security-label resource -> dinegasikan jadi PERMIT (by design)")
        void mismatchedSecurityLabel_isNegatedToPermit() {
            ProvisionComponent p = provision(ConsentProvisionType.DENY, "read", "Observation");
            p.addSecurityLabel(new Coding(ORG_SECURITY_SYSTEM, OTHER_ORG_ID, null));
            Consent consent = activeConsent(p);

            // Resource-nya security-label ORG_ID, tapi provision target OTHER_ORG_ID -> tidak match
            ConsentOutcome outcome = evaluator(consent)
                    .evaluate(observationWithOrgSecurityLabel(ORG_ID), ActionEnum.READ, new ArrayList<>());

            // Ini BUKAN bug - perilaku sengaja (negasi provisionOperationNeg), tapi
            // gampang disalahpahami. Kalau ini berubah jadi REJECT tanpa disengaja,
            // artinya ada regresi di checkSecurityLabelProvision().
            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus(),
                    "Security-label mismatch seharusnya di-negasikan jadi PERMIT, bukan tetap DENY atau NULL");
        }

        @Test
        @DisplayName("Resource tanpa security-label sama sekali, provision DENY punya security-label -> dinegasikan jadi PERMIT")
        void resourceWithoutSecurityLabel_isNegatedToPermit() {
            ProvisionComponent p = provision(ConsentProvisionType.DENY, "read", "Observation");
            p.addSecurityLabel(new Coding(ORG_SECURITY_SYSTEM, ORG_ID, null));
            Consent consent = activeConsent(p);

            Observation obsWithoutLabel = new Observation(); // tidak ada Meta.security

            ConsentOutcome outcome = evaluator(consent).evaluate(obsWithoutLabel, ActionEnum.READ, new ArrayList<>());

            assertEquals(ConsentOperationStatusEnum.PROCEED, outcome.getStatus());
        }
    }

    // ---------------------------------------------------------------------
    // Static operator functions (provisionOperationAnd/Or/Neg)
    // ---------------------------------------------------------------------

    @Nested
    @DisplayName("Static operator functions")
    class OperatorFunctions {

        @Test
        @DisplayName("provisionOperationAnd: PERMIT+PERMIT=PERMIT, DENY menang atas PERMIT, NULL tetap annihilator")
        void andOperator() {
            assertEquals(ConsentProvisionType.PERMIT,
                    PolicyEvaluator.provisionOperationAnd(ConsentProvisionType.PERMIT, ConsentProvisionType.PERMIT));
            assertEquals(ConsentProvisionType.DENY,
                    PolicyEvaluator.provisionOperationAnd(ConsentProvisionType.DENY, ConsentProvisionType.PERMIT));
            assertEquals(ConsentProvisionType.DENY,
                    PolicyEvaluator.provisionOperationAnd(ConsentProvisionType.PERMIT, ConsentProvisionType.DENY));
            assertEquals(ConsentProvisionType.DENY,
                    PolicyEvaluator.provisionOperationAnd(ConsentProvisionType.DENY, ConsentProvisionType.DENY));
            // NULL adalah annihilator di fungsi level ini (dijaga di level evaluate()
            // dengan cara skip decision NULL sebelum masuk ke fold, lihat FoldLogicRegression).
            assertEquals(ConsentProvisionType.NULL,
                    PolicyEvaluator.provisionOperationAnd(ConsentProvisionType.NULL, ConsentProvisionType.DENY));
            assertEquals(ConsentProvisionType.NULL,
                    PolicyEvaluator.provisionOperationAnd(ConsentProvisionType.PERMIT, ConsentProvisionType.NULL));
        }

        @Test
        @DisplayName("provisionOperationOr: NULL adalah identity, PERMIT menang atas DENY")
        void orOperator() {
            assertEquals(ConsentProvisionType.DENY,
                    PolicyEvaluator.provisionOperationOr(ConsentProvisionType.NULL, ConsentProvisionType.DENY));
            assertEquals(ConsentProvisionType.PERMIT,
                    PolicyEvaluator.provisionOperationOr(ConsentProvisionType.NULL, ConsentProvisionType.PERMIT));
            assertEquals(ConsentProvisionType.PERMIT,
                    PolicyEvaluator.provisionOperationOr(ConsentProvisionType.DENY, ConsentProvisionType.PERMIT));
            assertEquals(ConsentProvisionType.NULL,
                    PolicyEvaluator.provisionOperationOr(ConsentProvisionType.NULL, ConsentProvisionType.NULL));
        }

        @Test
        @DisplayName("provisionOperationNeg: PERMIT<->DENY terbalik, NULL tetap NULL")
        void negOperator() {
            assertEquals(ConsentProvisionType.DENY, PolicyEvaluator.provisionOperationNeg(ConsentProvisionType.PERMIT));
            assertEquals(ConsentProvisionType.PERMIT, PolicyEvaluator.provisionOperationNeg(ConsentProvisionType.DENY));
            assertEquals(ConsentProvisionType.NULL, PolicyEvaluator.provisionOperationNeg(ConsentProvisionType.NULL));
        }
    }
}
