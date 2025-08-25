package ch.ahdis.matchbox.terminology.providers;

import ca.uhn.fhir.rest.annotation.Operation;
import org.hl7.fhir.instance.model.api.IAnyResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OperationProvider {
	private static final Logger log = LoggerFactory.getLogger(OperationProvider.class);

	@Operation(
		name = "validate-code",
		canonicalUrl = "http://hl7.org/fhir/OperationDefinition/Resource-validate-code",
		idempotent = true
	)
	public IAnyResource validateCode() {
		log.info("OperationProvider::validate-code");
		return null;
	}

	@Operation(
		name = "batch-validate-code",
		canonicalUrl = "http://hl7.org/fhir/OperationDefinition/Resource-batch-validate-code",
		idempotent = true
	)
	public IAnyResource batchValidateCode() {
		log.info("OperationProvider::batch-validate-code");
		return null;
	}

	@Operation(
		name = "versions",
		canonicalUrl = "http://localhost/r4/OperationDefinition/fso-versions",
		idempotent = true
	)
	public IAnyResource versions() {
		log.info("OperationProvider::versions");
		return null;
	}
}
