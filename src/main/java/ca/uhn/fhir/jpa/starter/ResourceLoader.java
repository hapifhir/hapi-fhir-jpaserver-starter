package ca.uhn.fhir.jpa.starter;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.dao.TransactionProcessor;
import java.io.IOException;
import java.io.InputStream;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Service;

@Service
public class ResourceLoader {

	public ResourceLoader(FhirContext fhirContext, TransactionProcessor transactionProcessor,
		AppProperties appProperties)
		throws IOException {

		var resource = new DefaultResourceLoader().getResource(
			appProperties.getTransaction_file_path());

		try (InputStream inputStream = resource.getInputStream()) {
			var transactionBundle = fhirContext.newJsonParser()
				.parseResource(Bundle.class, inputStream);
			transactionProcessor.transaction(null, transactionBundle, false);
		}
	}
}
