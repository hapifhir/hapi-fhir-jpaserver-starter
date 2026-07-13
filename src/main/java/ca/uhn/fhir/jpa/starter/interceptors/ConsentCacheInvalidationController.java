package ca.uhn.fhir.jpa.starter.interceptors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint internal untuk invalidasi cache consent list per-organisasi.
 * <p>
 * Endpoint ini SENGAJA didaftarkan sebagai Spring {@code @RestController}
 * biasa (bukan bagian dari FHIR {@code RestfulServer}), supaya:
 * </p>
 * <ul>
 *   <li>Tidak melalui {@link ConsentEnforcementInterceptor} sama sekali
 *       (tidak butuh header {@code X-Organization-ID}).</li>
 *   <li>Path-nya terpisah dari base FHIR ({@code /fhir/*}), jadi tidak
 *       bentrok dengan resource FHIR apapun.</li>
 * </ul>
 * <p>
 * Dipanggil oleh consent registry (service Go terpisah) setiap ada
 * perubahan (create/update/delete) pada resource {@code Consent} atau
 * {@code Provision} di suatu organisasi, supaya cache 60 menit di
 * {@link ResourceManipulationInterceptor} tidak menyajikan data basi.
 * </p>
 * <p>
 * <b>Keamanan:</b> endpoint ini wajib menyertakan header
 * {@code X-Cache-Invalidation-Secret} yang cocok dengan
 * {@code hapi.policy.cache-invalidation-secret} (env var
 * {@code HAPI_POLICY_CACHE_INVALIDATION_SECRET}). Kalau secret belum
 * di-set di server (kosong), endpoint ini SELALU menolak request apapun
 * (fail-closed) - bukan fail-open - supaya tidak ada orang bisa memicu
 * cache-miss flood ke registry tanpa otentikasi.
 * </p>
 */
@RestController
@RequestMapping("/internal/consent-cache")
public class ConsentCacheInvalidationController {

    @Autowired
    private ResourceManipulationInterceptor resourceManipulationInterceptor;

    @Autowired
    private ConsentProperties consentProperties;

    public static class InvalidateRequest {
        private String organizationId;

        public String getOrganizationId() {
            return organizationId;
        }

        public void setOrganizationId(String organizationId) {
            this.organizationId = organizationId;
        }
    }

    public static class InvalidateResponse {
        private boolean success;
        private String message;
        private String organizationId;

        public InvalidateResponse(boolean success, String message, String organizationId) {
            this.success = success;
            this.message = message;
            this.organizationId = organizationId;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getOrganizationId() {
            return organizationId;
        }
    }

    @PostMapping("/invalidate")
    public ResponseEntity<InvalidateResponse> invalidate(
            @RequestHeader(value = "X-Cache-Invalidation-Secret", required = false) String secret,
            @RequestBody InvalidateRequest body) {

        String expectedSecret = consentProperties.getCacheInvalidationSecret();

        // Fail-closed: kalau secret belum dikonfigurasi di server, tolak semua request.
        if (expectedSecret == null || expectedSecret.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new InvalidateResponse(false,
                            "Cache invalidation secret belum dikonfigurasi di server (hapi.policy.cache-invalidation-secret kosong).",
                            null));
        }

        if (secret == null || !expectedSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new InvalidateResponse(false, "Invalid or missing X-Cache-Invalidation-Secret header.", null));
        }

        if (body == null || body.getOrganizationId() == null || body.getOrganizationId().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new InvalidateResponse(false, "Field organizationId wajib diisi.", null));
        }

        resourceManipulationInterceptor.invalidateConsentCache(body.getOrganizationId());

        return ResponseEntity.ok(new InvalidateResponse(true,
                "Cache consent list untuk organisasi " + body.getOrganizationId() + " berhasil di-invalidate.",
                body.getOrganizationId()));
    }
}
