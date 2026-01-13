package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.fhir.r4.model.Patient;
import ca.uhn.fhir.fhir.r5.model.Resource;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ValidationResult;
import ca.uhn.fhir.validation.SingleServerValidationSupport;
import ca.uhn.fhir.converter.FhirConverter;

import java.io.IOException;

public class FhirConverterExample {

    public static void main(String[] args) throws IOException {

        // 1. Setup HAPI FHIR context for R4 and R5
        FhirContext r4FhirContext = FhirContext.forR4();
        FhirContext r5FhirContext = FhirContext.forR5();
        IParser r4JsonParser = r4FhirContext.newJsonParser();
        IParser r5JsonParser = r5FhirContext.newJsonParser();

        // 2. Sample R4 resource (Patient)
        String r4ResourceJson = "{\n" + //
                "  \"category\": [\n" + //
                "    {\n" + //
                "      \"coding\": [\n" + //
                "        {\n" + //
                "          \"code\": \"exam\",\n" + //
                "          \"display\": \"Exam\",\n" + //
                "          \"system\": \"http://terminology.hl7.org/CodeSystem/observation-category\",\n" + //
                "          \"userSelected\": false\n" + //
                "        }\n" + //
                "      ],\n" + //
                "      \"text\": \"Exam\"\n" + //
                "    }\n" + //
                "  ],\n" + //
                "  \"code\": {\n" + //
                "    \"coding\": [\n" + //
                "      {\n" + //
                "        \"code\": \"159859\",\n" + //
                "        \"display\": \"Polymerase chain reaction, human papilloma virus, qualitative\",\n" + //
                "        \"system\": \"/orgs/CIEL/sources/CIEL/concepts/159859/\"\n" + //
                "      }\n" + //
                "    ],\n" + //
                "    \"text\": \"Polymerase chain reaction, human papilloma virus, qualitative\"\n" + //
                "  },\n" + //
                "  \"effectiveInstant\": \"2025-04-14T08:37:41Z\",\n" + //
                "  \"encounter\": {\n" + //
                "    \"id\": \"a9b13226-806d-46db-8d11-f1f8fbd7524d\",\n" + //
                "    \"reference\": \"Encounter/a9b13226-806d-46db-8d11-f1f8fbd7524d\"\n" + //
                "  },\n" + //
                "  \"id\": \"e2c94a0c-e573-44f1-9823-60b7b736b3f5\",\n" + //
                "  \"language\": \"EN\",\n" + //
                "  \"meta\": {\n" + //
                "    \"lastUpdated\": \"2025-04-14T08:37:43.411479+00:00\",\n" + //
                "    \"tag\": [\n" + //
                "      {\n" + //
                "        \"code\": \"31ec74c4-3e12-4b11-a3e9-8d2689c00350\",\n" + //
                "        \"display\": \"Empower\",\n" + //
                "        \"system\": \"http://mycarehub/tenant-identification/organisation\",\n" + //
                "        \"userSelected\": false,\n" + //
                "        \"version\": \"1.0\"\n" + //
                "      },\n" + //
                "      {\n" + //
                "        \"code\": \"20d6257c-dd31-4cf5-bfcc-42171808e97e\",\n" + //
                "        \"display\": \"Main Branch\",\n" + //
                "        \"system\": \"http://mycarehub/tenant-identification/facility\",\n" + //
                "        \"userSelected\": false,\n" + //
                "        \"version\": \"1.0\"\n" + //
                "      }\n" + //
                "    ],\n" + //
                "    \"versionId\": \"MTc0NDYxOTg2MzQxMTQ3OTAwMA\"\n" + //
                "  },\n" + //
                "  \"resourceType\": \"Observation\",\n" + //
                "  \"status\": \"final\",\n" + //
                "  \"subject\": {\n" + //
                "    \"display\": \"Mwai, Patience \",\n" + //
                "    \"id\": \"b2e36618-dee4-4108-b6b6-0a1e33d567c8\",\n" + //
                "    \"reference\": \"Patient/b2e36618-dee4-4108-b6b6-0a1e33d567c8\"\n" + //
                "  },\n" + //
                "  \"text\": {\n" + //
                "    \"div\": \"CERVICAL_CANCER_SCREENING\",\n" + //
                "    \"status\": \"additional\"\n" + //
                "  },\n" + //
                "  \"valueString\": \"positive\"\n" + //
                "}";
        Patient r4Patient = (Patient) r4JsonParser.parseResource(r4ResourceJson);

        // 3. Create a new converter
        FhirConverter converter = new FhirConverter();

        // 4. Convert the R4 Patient to R5
        Resource r5Resource = converter.convertResource(r4Patient, r5FhirContext);

        // 5. Serialize R5 resource to JSON
        String r5ResourceJson = r5JsonParser.encodeResourceToString(r5Resource);
        System.out.println(r5ResourceJson);

        // 6. Optional: Validate R5 resource against the R5 profiles
        FhirValidator validator = r5FhirContext.newValidator();
        validator.registerValidator(new SingleServerValidationSupport(r5FhirContext));

        ValidationResult validationResult = validator.validateResource(r5Resource);
        System.out.println(validationResult.isOk());

    }
}
