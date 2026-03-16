package ca.uhn.fhir.jpa.starter.interceptors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Consent;
import org.hl7.fhir.r4.model.Encounter;
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

    public <T extends IBaseResource> T getResource(String resourceId, Class<? extends T> clz, RequestDetails theRequestDetails) {
        if (resourceId == null)
            return null;

        CacheEnum cacheEnum = CacheEnum.fromResourceType(clz.getName());
        if (cacheEnum == null)
            return null;

        // Ambil dari Memory Cache jika ada
        T resource = cache.getIfPresent(cacheEnum, resourceId);
        if (resource == null) {
            // Ambil dari DaoRegistry
            resource = loadResourceFromDaoRegistry(resourceId, theRequestDetails);
        }

        if (resource != null) {
            // Simpan ke Cache
            cache.put(cacheEnum, resourceId, resource);
        }
        
        return resource;
    }

    private <T extends IBaseResource> T loadResourceFromDaoRegistry(String resourceId, RequestDetails theRequestDetails) {
        try {
            IFhirResourceDao<T> resourceDao = (IFhirResourceDao<T>) resourceDaoRegistry.getResourceDao(Encounter.class);
            return resourceDao.read(new IdType(resourceId), theRequestDetails);
        } catch (Exception e) {
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
                    ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
                    if (errorResponse.message != null) {
                        throw new Exception("[Err: " + errorResponse.errorCode + " : HTTP:" + errorResponse.httpCode + "]" + errorResponse.message);
                    }
                } else if (responseBody.startsWith("[")) {
                    Bundle bundle = fhirContext.newJsonParser().parseResource(
                        Bundle.class, responseBody);

                    for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
                        if (entry.getResource() instanceof Consent) {
                            consents.add((Consent) entry.getResource());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return consents;
    }
}
