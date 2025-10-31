package nl.hinq;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.*;

import java.util.Optional;

@Slf4j
public class NviInterceptor {

	private static final String URA_SYSTEM = "http://fhir.nl/fhir/NamingSystem/ura";
	private static final String BSN_SYSTEM = "http://fhir.nl/fhir/NamingSystem/bsn";
	private static final String DOCUMENT_REFERENCE_PROFILE = "http://nuts-foundation.github.io/nl-generic-functions-ig/StructureDefinition/nl-gf-localization-documentreference";
	private static final String DOCUMENT_REFERENCE_DATA = "This DocumentReference does not point to a specific document, but rather to documents or data in general at the custodian referenced in this instance. This can be used by data localization services (also known as medical record localization services).";
	// In practice this would be configured in properties
	private static final String ORG_URA = "90000697";
	private static final String ORG_TYPE = "Z3";
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

			DocumentReference documentReference = createNviEntry(optionalBsn.get());
			postDocumentReference(requestDetails, documentReference);
			log.info("Posted DocumentReference to NVI for patient with ID: {}", newResource.getIdElement().getIdPart());

			Subscription subscription = createMitzSubscription(optionalBsn.get());
			postSubscription(requestDetails, subscription);
			log.info("Posted Subscription to Mitz for patient with ID: {}", newResource.getIdElement().getIdPart());
		}
	}

	private void postDocumentReference(RequestDetails requestDetails, DocumentReference docRef) {
		FhirContext context = requestDetails.getFhirContext();
		var factory = context.getRestfulClientFactory();
		factory.setServerValidationMode(ServerValidationModeEnum.NEVER);
		IGenericClient client = factory.newGenericClient(NVI_ENDPOINT);
		try {
			client.create()
				.resource(docRef)
				.withAdditionalHeader("X-Tenant-Id", URA_SYSTEM+"|"+ORG_URA)
				.execute();
		} catch (Exception e) {
			log.error("Error posting DocumentReference to NVI: {}", e.getMessage());
			throw e;
		}
	}

	private DocumentReference createNviEntry(String bsn) {
		DocumentReference docRef = new DocumentReference();
		// Add profile
		docRef.getMeta().addProfile(DOCUMENT_REFERENCE_PROFILE);
		// Add BSN
		Identifier bsnIdentifier = new Identifier();
		bsnIdentifier.setSystem(BSN_SYSTEM);
		bsnIdentifier.setValue(bsn);
		docRef.addIdentifier(bsnIdentifier);
		// Set status
		docRef.setStatus(Enumerations.DocumentReferenceStatus.CURRENT);
		// Add type
		Coding typeCoding = new Coding();
		typeCoding.setSystem("http://loinc.org");
		typeCoding.setCode("55188-7");
		typeCoding.setDisplay("Patient data Document");
		CodeableConcept type = new CodeableConcept();
		type.addCoding(typeCoding);
		docRef.setType(type);
		// Set custodian
		Identifier uraIdentifier = new Identifier();
		uraIdentifier.setSystem(URA_SYSTEM);
		uraIdentifier.setValue(ORG_URA);
		Reference custodianReference = new Reference();
		custodianReference.setIdentifier(uraIdentifier);
		docRef.setCustodian(custodianReference);
		// Set content
		Attachment attachment = new Attachment();
		attachment.setContentType("text/plain");
		attachment.setData(java.util.Base64.getEncoder().encode(DOCUMENT_REFERENCE_DATA.getBytes()));
		attachment.setTitle("Generic reference to patient data");
		docRef.addContent().setAttachment(attachment);
		return docRef;
	}

	private void postSubscription(RequestDetails requestDetails, Subscription subscription) {
		FhirContext context = requestDetails.getFhirContext();
		var factory = context.getRestfulClientFactory();
		factory.setServerValidationMode(ServerValidationModeEnum.NEVER);
		IGenericClient client = factory.newGenericClient(MITZ_ENDPOINT);
		try {
			client.create()
				.resource(subscription)
				.execute();
		} catch (Exception e) {
			log.error("Error posting Subscription to Mitz: {}", e.getMessage());
			throw e;
		}
	}

	private Subscription createMitzSubscription(String bsn) {
		Subscription subscription = new Subscription();
		subscription.setStatus(Subscription.SubscriptionStatus.REQUESTED);
		subscription.setReason("OTV");
		// Set criteria to match DocumentReference for the given BSN
		subscription.setCriteria(String.format("Consent?_query=otv&patientid=%s&providerid=%s&providertype=%s",
			bsn, ORG_URA, ORG_TYPE));
		// Set channel
		Subscription.SubscriptionChannelComponent channel = new Subscription.SubscriptionChannelComponent();
		channel.setType(Subscription.SubscriptionChannelType.RESTHOOK);
		channel.setPayload("application/fhir+json");
		subscription.setChannel(channel);
		return subscription;
	}
}
