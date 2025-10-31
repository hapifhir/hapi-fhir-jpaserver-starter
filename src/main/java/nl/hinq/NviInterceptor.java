package nl.hinq;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import lombok.extern.slf4j.Slf4j;

import org.apache.jena.atlas.lib.InternalErrorException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.util.Optional;

@Slf4j
public class NviInterceptor {

	private static final String URA_SYSTEM = "http://fhir.nl/fhir/NamingSystem/ura";
	private static final String BSN_SYSTEM = "http://fhir.nl/fhir/NamingSystem/bsn";
	private static final String ORG_URA = "90000697";
	private static final String NVI_ENDPOINT = "http://dev-nuts-hackaton-source-nuts-knooppunt.dev-nuts-hackathon-source.svc.cluster.local:8081/nvi";
	private static final String MITZ_ENDPOINT = "http://dev-nuts-hackaton-source-nuts-knooppunt.dev-nuts-hackathon-source.svc.cluster.local:8081/mitz";

	@Hook(Pointcut.STORAGE_PRECOMMIT_RESOURCE_CREATED)
	public void resourceCreated(RequestDetails requestDetails, IBaseResource newResource) {
		if (newResource instanceof Patient patient) {
			log.info("New patient created with ID: {}", newResource.getIdElement().getIdPart());
			Optional<String> optionalBsn = patient.getIdentifier().stream()
				.filter(id -> BSN_SYSTEM.equals(id.getSystem()))
				.findAny()
				.map(Identifier::getValue);

			if (optionalBsn.isEmpty()) {
				log.warn("Patient does not have a BSN identifier.");
				return;
			}

			String documentReference = DOCUMENT_REFERENCE.formatted(optionalBsn.get(), ORG_URA);
			postDocumentReference(requestDetails, documentReference);
			log.info("Posted DocumentReference to NVI for patient with ID: {}", newResource.getIdElement().getIdPart());

			String subscription = SUBSCRIPTION.formatted(optionalBsn.get(), ORG_URA);
			postSubscription(requestDetails, subscription);
			log.info("Posted Subscription to Mitz for patient with ID: {}", newResource.getIdElement().getIdPart());
		}
	}

	private void postDocumentReference(RequestDetails requestDetails, String docRef) {
		FhirContext context = requestDetails.getFhirContext();
		var factory = context.getRestfulClientFactory();
		factory.setServerValidationMode(ServerValidationModeEnum.NEVER);
		IGenericClient client = factory.newGenericClient(NVI_ENDPOINT);
		try {
			client.create()
				.resource(docRef)
				.withAdditionalHeader("X-Tenant-Id", URA_SYSTEM+"|"+ORG_URA)
				.withAdditionalHeader("Content-Type", "application/fhir+json")
				.execute();
		} catch (Exception e) {
			String message = "Error posting DocumentReference to NVI: %s".formatted(e.getMessage());
			log.error(message);
			throw new InternalErrorException(message);
		}
	}

	private void postSubscription(RequestDetails requestDetails, String subscription) {
		FhirContext context = requestDetails.getFhirContext();
		var factory = context.getRestfulClientFactory();
		factory.setServerValidationMode(ServerValidationModeEnum.NEVER);
		IGenericClient client = factory.newGenericClient(MITZ_ENDPOINT);
		try {
			client.create()
				.resource(subscription)
				.withAdditionalHeader("Content-Type", "application/fhir+json")
				.execute();
		} catch (Exception e) {
			String message = "Error posting Subscription to Mitz: %s".formatted(e.getMessage());
			log.error(message);
			throw new InternalErrorException(message);
		}
	}

	private static String DOCUMENT_REFERENCE = "{\n" + //
				"  \"resourceType\" : \"DocumentReference\",\n" + //
				"  \"meta\" : {\n" + //
				"    \"profile\" : [\n" + //
				"      \"http://nuts-foundation.github.io/nl-generic-functions-ig/StructureDefinition/nl-gf-localization-documentreference\"\n" + //
				"    ]\n" + //
				"  },\n" + //
				"  \"status\" : \"current\",\n" + //
				"  \"type\" : {\n" + //
				"    \"coding\" : [\n" + //
				"      {\n" + //
				"        \"system\" : \"http://loinc.org\",\n" + //
				"        \"code\" : \"55188-7\",\n" + //
				"        \"display\" : \"Patient data Document\"\n" + //
				"      }\n" + //
				"    ]\n" + //
				"  },\n" + //
				"  \"subject\" : {\n" + //
				"    \"identifier\" : {\n" + //
				"      \"system\" : \"http://fhir.nl/fhir/NamingSystem/bsn\",\n" + //
				"      \"value\" : \"%s\"\n" + //
				"    }\n" + //
				"  },\n" + //
				"  \"custodian\" : {\n" + //
				"    \"identifier\" : {\n" + //
				"      \"system\" : \"http://fhir.nl/fhir/NamingSystem/ura\",\n" + //
				"      \"value\" : \"%s\"\n" + //
				"    }\n" + //
				"  },\n" + //
				"  \"content\" : [\n" + //
				"    {\n" + //
				"      \"attachment\" : {\n" + //
				"        \"contentType\" : \"text/plain\",\n" + //
				"        \"data\" : \"IlRoaXMgRG9jdW1lbnRSZWZlcmVuY2UgZG9lcyBub3QgcG9pbnQgdG8gYSBzcGVjaWZpYyBkb2N1bWVudCwgYnV0IHJhdGhlciB0byBkb2N1bWVudHMgb3IgZGF0YSBpbiBnZW5lcmFsIGF0IHRoZSBjdXN0b2RpYW4gcmVmZXJlbmNlZCBpbiB0aGlzIGluc3RhbmNlLiBUaGlzIGNhbiBiZSB1c2VkIGJ5IGRhdGEgbG9jYWxpemF0aW9uIHNlcnZpY2VzIChhbHNvIGtub3duIGFzIG1lZGljYWwgcmVjb3JkIGxvY2FsaXphdGlvbiBzZXJ2aWNlcykuIg==\",\n" + //
				"        \"title\" : \"Generic reference to patient data\"\n" + //
				"      }\n" + //
				"    }\n" + //
				"  ]\n" + //
				"}";

	private static String SUBSCRIPTION = "{\n" + //
				"    \"resourceType\": \"Subscription\",\n" + //
				"    \"status\": \"requested\",\n" + //
				"    \"reason\": \"OTV\",\n" + //
				"    \"criteria\": \"Consent?_query=otv&patientid=%s&providerid=%s&providertype=Z3\",\n" + //
				"    \"channel\": {\n" + //
				"        \"type\": \"rest-hook\",\n" + //
				"        \"payload\": \"application/fhir+json\"\n" + //
				"    }\n" + //
				"}";
}
