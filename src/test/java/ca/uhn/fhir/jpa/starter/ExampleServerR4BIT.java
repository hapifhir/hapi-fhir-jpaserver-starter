package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4b.model.Bundle;
import org.hl7.fhir.r4b.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = {Application.class},
	properties = {
		"spring.datasource.url=jdbc:h2:mem:dbr4b",
		"hapi.fhir.enable_repository_validating_interceptor=true",
		"spring.jpa.properties.hibernate.search.backend.directory.type=local-heap",
		"hapi.fhir.fhir_version=r4b",
		"hapi.fhir.subscription.websocket_enabled=false",
		"hapi.fhir.mdm_enabled=false",
		"hapi.fhir.cr_enabled=false",
		// Override is currently required when using MDM as the construction of the MDM
		// beans are ambiguous as they are constructed multiple places. This is evident
		// when running in a spring boot environment
		"spring.main.allow-bean-definition-overriding=true"})
class ExampleServerR4BIT {
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerR4BIT.class);
	private IGenericClient ourClient;
	private FhirContext ourCtx;

	@LocalServerPort
	private int port;

	@Test
	@Order(0)
	void testCreateAndRead() {
		String methodName = "testCreateAndRead";
		ourLog.info("Entering {}()...", methodName);

		Patient pt = new Patient();
		pt.setActive(true);
		pt.getBirthDateElement().setValueAsString("2020-01-01");
		pt.addIdentifier().setSystem("http://foo").setValue("12345");
		pt.addName().setFamily(methodName);
		IIdType id = ourClient.create().resource(pt).execute().getId();

		Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
		assertEquals(methodName, pt2.getName().get(0).getFamily());

	}


	@Test
	void testBatchPutWithIdenticalTags() {
		String batchPuts = """
			{
			\t"resourceType": "Bundle",
			\t"id": "patients",
			\t"type": "batch",
			\t"entry": [
			\t\t{
			\t\t\t"request": {
			\t\t\t\t"method": "PUT",
			\t\t\t\t"url": "Patient/pat-1"
			\t\t\t},
			\t\t\t"resource": {
			\t\t\t\t"resourceType": "Patient",
			\t\t\t\t"id": "pat-1",
			\t\t\t\t"meta": {
			\t\t\t\t\t"tag": [
			\t\t\t\t\t\t{
			\t\t\t\t\t\t\t"system": "http://mysystem.org",
			\t\t\t\t\t\t\t"code": "value2"
			\t\t\t\t\t\t}
			\t\t\t\t\t]
			\t\t\t\t}
			\t\t\t},
			\t\t\t"fullUrl": "/Patient/pat-1"
			\t\t},
			\t\t{
			\t\t\t"request": {
			\t\t\t\t"method": "PUT",
			\t\t\t\t"url": "Patient/pat-2"
			\t\t\t},
			\t\t\t"resource": {
			\t\t\t\t"resourceType": "Patient",
			\t\t\t\t"id": "pat-2",
			\t\t\t\t"meta": {
			\t\t\t\t\t"tag": [
			\t\t\t\t\t\t{
			\t\t\t\t\t\t\t"system": "http://mysystem.org",
			\t\t\t\t\t\t\t"code": "value2"
			\t\t\t\t\t\t}
			\t\t\t\t\t]
			\t\t\t\t}
			\t\t\t},
			\t\t\t"fullUrl": "/Patient/pat-2"
			\t\t}
			\t]
			}""";
		Bundle bundle = FhirContext.forR4B().newJsonParser().parseResource(Bundle.class, batchPuts);
		ourClient.transaction().withBundle(bundle).execute();
	}


	@BeforeEach
	void beforeEach() {

		ourCtx = FhirContext.forR4B();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);


	}
}
