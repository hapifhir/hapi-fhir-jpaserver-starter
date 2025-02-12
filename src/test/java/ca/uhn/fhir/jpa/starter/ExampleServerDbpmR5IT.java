package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.migrate.DriverTypeEnum;
import ca.uhn.fhir.jpa.migrate.JdbcUtils;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class}, properties =
	{
		"spring.datasource.url=jdbc:h2:mem:dbr5_dbpm",
		"hapi.fhir.fhir_version=r5",
		"hapi.fhir.partitioning.database_partition_mode_enabled=true",
		"hapi.fhir.partitioning.patient_id_partitioning_mode=true"
	})
public class ExampleServerDbpmR5IT {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ExampleServerDstu2IT.class);
	private IGenericClient ourClient;
	private FhirContext ourCtx;


	@LocalServerPort
	private int port;

	@Autowired
	private DataSource myDataSource;

	@Autowired
	private PlatformTransactionManager myTxManager;


	@Test
	void testCreateAndRead() {
		Patient pt = new Patient();
		pt.setId("A");
		pt.setActive(true);
		IIdType id = ourClient.update().resource(pt).execute().getId();

		Patient pt2 = ourClient.read().resource(Patient.class).withId("Patient/A").execute();
		assertTrue(pt2.getActive());
	}


	@Test
	public void testValidateSchema() throws SQLException {
		TransactionTemplate tt = new TransactionTemplate(myTxManager);
		DriverTypeEnum.ConnectionProperties cp = new DriverTypeEnum.ConnectionProperties(myDataSource, tt, DriverTypeEnum.H2_EMBEDDED);
		Set<String> columns = JdbcUtils.getPrimaryKeyColumns(cp, "HFJ_RESOURCE");
		assertThat(columns).containsExactlyInAnyOrder("RES_ID", "PARTITION_ID");
	}


	@BeforeEach
	void beforeEach() {

		ourCtx = FhirContext.forR5();
		ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String ourServerBase = "http://localhost:" + port + "/fhir/";
		ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
		ourClient.registerInterceptor(new LoggingInterceptor(true));
	}
}
