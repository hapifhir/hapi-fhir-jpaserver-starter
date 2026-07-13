package ca.uhn.fhir.jpa.starter.interceptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.IdType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.starter.interceptors.CacheService.CacheEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;

public class LoaderWithCache {
    private CacheService cache = new CacheService();
    private ConsentProperties consentProperties;
    private DaoRegistry resourceDaoRegistry;
    private FhirContext fhirContext;
    private RestTemplate restTemplate = new RestTemplate();

    public LoaderWithCache(ConsentProperties consentProperties) {
        this.consentProperties = consentProperties;
    }

    public void setResourceDaoRegistry(DaoRegistry resourceDaoRegistry) {
        this.resourceDaoRegistry = resourceDaoRegistry;
    }

    public void setFhirContext(FhirContext fhirContext) {
        this.fhirContext = fhirContext;
    }

    /**
     * Invalidate cache consent list utk satu organisasi. Dipanggil dari luar
     * (lewat endpoint invalidasi) setiap ada perubahan Consent/Provision di
     * registry, supaya request berikutnya fetch data fresh, bukan cache lama.
     */
    public void invalidateConsentList(String orgId) {
        cache.invalidate(CacheEnum.CONSENT_LIST, orgId);
    }

    public <T extends IBaseResource> T getResource(String resourceId, Class<? extends T> clz, RequestDetails theRequestDetails) {
        if (resourceId == null)
            return null;

        CacheEnum cacheEnum = CacheEnum.fromResourceType(clz.getSimpleName());
        if (cacheEnum == null)
            return null;

        // Ambil dari Memory Cache jika ada
        T resource = cache.getIfPresent(cacheEnum, resourceId);
        if (resource == null) {
            // Ambil dari DaoRegistry
            resource = loadResourceFromDaoRegistry(resourceId, clz, theRequestDetails);
        }

        if (resource != null) {
            // Simpan ke Cache
            cache.put(cacheEnum, resourceId, resource);
        }
        
        return resource;
    }

    private <T extends IBaseResource> T loadResourceFromDaoRegistry(String resourceId, Class<? extends T> clz, RequestDetails theRequestDetails) {
        try {
            IFhirResourceDao<T> resourceDao = (IFhirResourceDao<T>) resourceDaoRegistry.getResourceDao(clz);
            return resourceDao.read(new IdType(resourceId), theRequestDetails);
        } catch (Exception e) {
            // Jangan ditelan diam-diam - kalau tidak, resource yang gagal di-fetch
            // akan selalu terlihat seperti "tidak ditemukan" tanpa penjelasan.
            e.printStackTrace();
            return null;
        }
    }

    public List<Consent> getConsentList(String env, String orgId) {
        // Ambil dari Memory Cache jika ada
        List<Consent> consents = cache.getIfPresent(CacheEnum.CONSENT_LIST, orgId);
        if (consents == null) {
            // Ambil dari ConsentRegistry
            consents = loadConsentListFromRegistry(env, orgId);

            if (consents != null && consents.size() > 0) {
                // Ambil dari Consent Registry
                cache.put(CacheEnum.CONSENT_LIST, orgId, consents);
            }
        }

        return consents;
    }
    
    private List<Consent> loadConsentListFromRegistry(String env, String orgId) {
        List<Consent> consents = new ArrayList<Consent>();

        try {
            String url = consentProperties.getUrlConsentRegistry() + "/search";

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");

            // Create JSON payload
            Map<String, String> payload = new HashMap<>();
            payload.put("env", env);
            payload.put("organization", orgId);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPayload = objectMapper.writeValueAsString(payload);

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                String responseBody = response.getBody();

                if (responseBody.startsWith("{")) {
                    com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(responseBody);
                    String resourceType = rootNode.has("resourceType") ? rootNode.get("resourceType").asText() : null;

                    if ("Bundle".equals(resourceType)) {
                        // Support format Bundle (mis. hasil endpoint lain / versi registry lain)
                        Bundle bundle = fhirContext.newJsonParser().parseResource(Bundle.class, responseBody);
                        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                            if (entry.getResource() instanceof Consent) {
                                consents.add((Consent) entry.getResource());
                            }
                        }
                    } else if ("Consent".equals(resourceType)) {
                        // Support format single resource Consent (bukan array/bundle)
                        Consent consent = fhirContext.newJsonParser().parseResource(Consent.class, responseBody);
                        consents.add(consent);
                    } else {
                        ErrorResponse errorResponse = objectMapper.treeToValue(rootNode, ErrorResponse.class);
                        if (errorResponse.message != null) {
                            throw new Exception("[Err: " + errorResponse.errorCode + " : HTTP:" + errorResponse.httpCode + "]" + errorResponse.message);
                        }
                    }
                } else if (responseBody.startsWith("[")) {
                    // Consent registry mengembalikan array JSON polos berisi resource Consent
                    // (bukan FHIR Bundle), jadi setiap elemen di-parse satu per satu.
                    com.fasterxml.jackson.databind.JsonNode arrayNode = objectMapper.readTree(responseBody);
                    for (com.fasterxml.jackson.databind.JsonNode node : arrayNode) {
                        Consent consent = fhirContext.newJsonParser().parseResource(Consent.class, node.toString());
                        consents.add(consent);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return consents;
    }
}
