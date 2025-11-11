package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.binstore.FilesystemBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.dao.data.IBinaryStorageEntityDao;
import ca.uhn.fhir.jpa.model.entity.BinaryStorageEntity;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

abstract class BaseBinaryStorageIntegrationTest {
	protected static final String COMMON_CONFIG_LOCATION = "spring.config.location=classpath:/binary-storage-test-empty.yaml";
	protected static final String COMMON_H2_USERNAME = "spring.datasource.username=sa";
	protected static final String COMMON_H2_PASSWORD = "spring.datasource.password=";
	protected static final String COMMON_JPA_DDL = "spring.jpa.hibernate.ddl-auto=create-drop";
	protected static final String COMMON_HIBERNATE_DIALECT =
		"spring.jpa.properties.hibernate.dialect=ca.uhn.fhir.jpa.model.dialect.HapiFhirH2Dialect";
	protected static final String COMMON_HIBERNATE_SEARCH_DISABLED = "spring.jpa.properties.hibernate.search.enabled=false";
	protected static final String COMMON_FLYWAY_DISABLED = "spring.flyway.enabled=false";
	protected static final String COMMON_FHIR_VERSION = "hapi.fhir.fhir_version=r4";
	protected static final String COMMON_REPO_VALIDATION_DISABLED =
		"hapi.fhir.enable_repository_validating_interceptor=false";
	protected static final String COMMON_MDM_DISABLED = "hapi.fhir.mdm_enabled=false";
	protected static final String COMMON_CR_DISABLED = "hapi.fhir.cr_enabled=false";
	protected static final String COMMON_SUBSCRIPTION_WS_DISABLED = "hapi.fhir.subscription.websocket_enabled=false";
	protected static final String COMMON_BEAN_OVERRIDE_ALLOWED = "spring.main.allow-bean-definition-overriding=true";
	protected static final String COMMON_CIRCULAR_REFERENCES = "spring.main.allow-circular-references=true";
	protected static final String COMMON_MCP_DISABLED = "spring.ai.mcp.server.enabled=false";
	protected static final String CONTENT_TYPE = "application/octet-stream";

	@LocalServerPort
	protected int port;

	protected FhirContext fhirContext;
	protected IGenericClient client;
	private final List<IIdType> resourcesToDelete = new ArrayList<>();

	@BeforeEach
	void setUpClient() {
		fhirContext = FhirContext.forR4();
		fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
		fhirContext.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
		String serverBase = "http://localhost:" + port + "/fhir/";
		client = fhirContext.newRestfulGenericClient(serverBase);
		resourcesToDelete.clear();
	}

	@AfterEach
	void deleteCreatedResources() {
		for (IIdType id : resourcesToDelete) {
			try {
				client.delete().resourceById(id).execute();
			} catch (Exception ignored) {
				// Ignore cleanup failures to keep tests resilient
			}
		}
	}

	protected IIdType createPatientWithPhoto(String label, byte[] payload) {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:binary-storage-test").setValue(label);
		patient.addName().setFamily(label);
		patient.addPhoto().setContentType(CONTENT_TYPE).setData(payload);
		IIdType id = client.create().resource(patient).execute().getId().toUnqualifiedVersionless();
		resourcesToDelete.add(id);
		return id;
	}

	protected String uniqueLabel(String prefix) {
		return prefix + "-" + UUID.randomUUID();
	}

	protected byte[] randomBytes(int size) {
		byte[] payload = new byte[size];
		ThreadLocalRandom.current().nextBytes(payload);
		return payload;
	}

	protected void assertRegularFileCount(Path baseDir, long expectedFileCount) throws IOException {
		assertThat(regularFileCount(baseDir)).isEqualTo(expectedFileCount);
	}

	protected void assertRegularFileCountGreaterThan(Path baseDir, long minimumFileCount) throws IOException {
		assertThat(regularFileCount(baseDir)).isGreaterThan(minimumFileCount);
	}

	protected Path ensureDirectory(Path directory) throws IOException {
		Files.createDirectories(directory);
		return directory;
	}

	protected void deleteDirectoryContents(Path baseDir) throws IOException {
		if (Files.notExists(baseDir)) {
			return;
		}
		try (Stream<Path> files = Files.walk(baseDir)) {
			files.sorted(Comparator.reverseOrder())
					.filter(path -> !path.equals(baseDir))
					.forEach(path -> {
						try {
							Files.deleteIfExists(path);
						} catch (IOException e) {
							throw new UncheckedIOException(e);
						}
					});
		}
	}

	private long regularFileCount(Path baseDir) throws IOException {
		if (Files.notExists(baseDir)) {
			return 0;
		}
		try (Stream<Path> files = Files.walk(baseDir)) {
			return files.filter(Files::isRegularFile).count();
		}
	}
}

@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = Application.class,
	properties = {
		BaseBinaryStorageIntegrationTest.COMMON_CONFIG_LOCATION,
		"spring.datasource.url=jdbc:h2:mem:binary-storage-db;DB_CLOSE_DELAY=-1",
		BaseBinaryStorageIntegrationTest.COMMON_H2_USERNAME,
		BaseBinaryStorageIntegrationTest.COMMON_H2_PASSWORD,
		BaseBinaryStorageIntegrationTest.COMMON_JPA_DDL,
		BaseBinaryStorageIntegrationTest.COMMON_HIBERNATE_DIALECT,
		BaseBinaryStorageIntegrationTest.COMMON_HIBERNATE_SEARCH_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_FLYWAY_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_FHIR_VERSION,
		BaseBinaryStorageIntegrationTest.COMMON_REPO_VALIDATION_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_MDM_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_CR_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_SUBSCRIPTION_WS_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_BEAN_OVERRIDE_ALLOWED,
		BaseBinaryStorageIntegrationTest.COMMON_CIRCULAR_REFERENCES,
		BaseBinaryStorageIntegrationTest.COMMON_MCP_DISABLED,
		"hapi.fhir.binary_storage_enabled=true",
		"hapi.fhir.binary_storage_mode=DATABASE"
	}
)
class BinaryStorageDatabaseModeIT extends BaseBinaryStorageIntegrationTest {

	@Autowired
	private IBinaryStorageEntityDao binaryStorageEntityDao;

	@Autowired
	private PlatformTransactionManager transactionManager;

	private TransactionTemplate transactionTemplate;

	@BeforeEach
	void initTemplate() {
		transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Test
	void largeAttachmentStoredInDatabase() {
		Set<String> beforeIds = captureContentIds();

		createPatientWithPhoto(uniqueLabel("database"), randomBytes(150_000));

		Set<String> afterIds = captureContentIds();
		afterIds.removeAll(beforeIds);
		assertThat(afterIds).hasSize(1);

		String binaryId = afterIds.iterator().next();
		BinaryStorageEntity entity = transactionTemplate.execute(status ->
			binaryStorageEntityDao.findById(binaryId).orElseThrow());

		assertThat(entity.hasStorageContent()).isTrue();
		assertThat(entity.getStorageContentBin()).hasSize(150_000);

		transactionTemplate.execute(status -> {
			binaryStorageEntityDao.deleteById(binaryId);
			return null;
		});
	}

	private Set<String> captureContentIds() {
		return transactionTemplate.execute(status ->
			binaryStorageEntityDao.findAll().stream()
					.map(BinaryStorageEntity::getContentId)
					.collect(Collectors.toCollection(LinkedHashSet::new)));
	}
}

@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = Application.class,
	properties = {
		BaseBinaryStorageIntegrationTest.COMMON_CONFIG_LOCATION,
		"spring.datasource.url=jdbc:h2:mem:binary-storage-fs-default;DB_CLOSE_DELAY=-1",
		BaseBinaryStorageIntegrationTest.COMMON_H2_USERNAME,
		BaseBinaryStorageIntegrationTest.COMMON_H2_PASSWORD,
		BaseBinaryStorageIntegrationTest.COMMON_JPA_DDL,
		BaseBinaryStorageIntegrationTest.COMMON_HIBERNATE_DIALECT,
		BaseBinaryStorageIntegrationTest.COMMON_HIBERNATE_SEARCH_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_FLYWAY_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_FHIR_VERSION,
		BaseBinaryStorageIntegrationTest.COMMON_REPO_VALIDATION_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_MDM_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_CR_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_SUBSCRIPTION_WS_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_BEAN_OVERRIDE_ALLOWED,
		BaseBinaryStorageIntegrationTest.COMMON_CIRCULAR_REFERENCES,
		BaseBinaryStorageIntegrationTest.COMMON_MCP_DISABLED,
		"hapi.fhir.binary_storage_enabled=true",
		"hapi.fhir.binary_storage_mode=FILESYSTEM",
		"hapi.fhir.binary_storage_filesystem_base_directory=target/test-binary-storage/filesystem-default"
	}
)
class BinaryStorageFilesystemDefaultIT extends BaseBinaryStorageIntegrationTest {
	static final Path BASE_DIRECTORY = Paths.get("target/test-binary-storage/filesystem-default").toAbsolutePath();

	@Autowired
	private FilesystemBinaryStorageSvcImpl filesystemBinaryStorageSvc;

	@BeforeEach
	void prepareDirectory() throws IOException {
		ensureDirectory(BASE_DIRECTORY);
		deleteDirectoryContents(BASE_DIRECTORY);
	}

	@Test
	void filesystemModeUsesDefaultThreshold() throws IOException {
		assertThat(filesystemBinaryStorageSvc.getMinimumBinarySize()).isEqualTo(102_400);
		assertRegularFileCount(BASE_DIRECTORY, 0);

		createPatientWithPhoto(uniqueLabel("fs-default-inline"), randomBytes(50_000));
		assertRegularFileCount(BASE_DIRECTORY, 0);

		createPatientWithPhoto(uniqueLabel("fs-default-offload"), randomBytes(150_000));
		assertRegularFileCountGreaterThan(BASE_DIRECTORY, 0);
	}

	@AfterEach
	void cleanUpDirectory() throws IOException {
		deleteDirectoryContents(BASE_DIRECTORY);
	}
}

@ActiveProfiles("test")
@SpringBootTest(
	webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
	classes = Application.class,
	properties = {
		BaseBinaryStorageIntegrationTest.COMMON_CONFIG_LOCATION,
		"spring.datasource.url=jdbc:h2:mem:binary-storage-fs-custom;DB_CLOSE_DELAY=-1",
		BaseBinaryStorageIntegrationTest.COMMON_H2_USERNAME,
		BaseBinaryStorageIntegrationTest.COMMON_H2_PASSWORD,
		BaseBinaryStorageIntegrationTest.COMMON_JPA_DDL,
		BaseBinaryStorageIntegrationTest.COMMON_HIBERNATE_DIALECT,
		BaseBinaryStorageIntegrationTest.COMMON_HIBERNATE_SEARCH_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_FLYWAY_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_FHIR_VERSION,
		BaseBinaryStorageIntegrationTest.COMMON_REPO_VALIDATION_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_MDM_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_CR_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_SUBSCRIPTION_WS_DISABLED,
		BaseBinaryStorageIntegrationTest.COMMON_BEAN_OVERRIDE_ALLOWED,
		BaseBinaryStorageIntegrationTest.COMMON_CIRCULAR_REFERENCES,
		BaseBinaryStorageIntegrationTest.COMMON_MCP_DISABLED,
		"hapi.fhir.binary_storage_enabled=true",
		"hapi.fhir.binary_storage_mode=FILESYSTEM",
		"hapi.fhir.binary_storage_filesystem_base_directory=target/test-binary-storage/filesystem-custom",
		"hapi.fhir.inline_resource_storage_below_size=32768"
	}
)
class BinaryStorageFilesystemCustomThresholdIT extends BaseBinaryStorageIntegrationTest {
	static final Path BASE_DIRECTORY = Paths.get("target/test-binary-storage/filesystem-custom").toAbsolutePath();

	@Autowired
	private FilesystemBinaryStorageSvcImpl filesystemBinaryStorageSvc;

	@BeforeEach
	void prepareDirectory() throws IOException {
		ensureDirectory(BASE_DIRECTORY);
		deleteDirectoryContents(BASE_DIRECTORY);
	}

	@Test
	void filesystemModeHonoursCustomThreshold() throws IOException {
		assertThat(filesystemBinaryStorageSvc.getMinimumBinarySize()).isEqualTo(32_768);
		assertRegularFileCount(BASE_DIRECTORY, 0);

		createPatientWithPhoto(uniqueLabel("fs-custom-inline"), randomBytes(30_000));
		assertRegularFileCount(BASE_DIRECTORY, 0);

		createPatientWithPhoto(uniqueLabel("fs-custom-offload"), randomBytes(40_000));
		assertRegularFileCountGreaterThan(BASE_DIRECTORY, 0);
	}

	@AfterEach
	void cleanUpDirectory() throws IOException {
		deleteDirectoryContents(BASE_DIRECTORY);
	}
}
