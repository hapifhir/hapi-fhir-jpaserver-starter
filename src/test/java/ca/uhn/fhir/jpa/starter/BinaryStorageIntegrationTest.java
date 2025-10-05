package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.binary.api.IBinaryStorageSvc;
import ca.uhn.fhir.jpa.binary.api.StoredDetails;
import ca.uhn.fhir.jpa.binstore.DatabaseBinaryContentStorageSvcImpl;
import ca.uhn.fhir.jpa.binstore.FilesystemBinaryStorageSvcImpl;
import ca.uhn.fhir.jpa.dao.data.IBinaryStorageEntityDao;
import ca.uhn.fhir.jpa.model.entity.BinaryStorageEntity;
import ca.uhn.fhir.rest.api.server.SystemRequestDetails;
import org.hl7.fhir.r4.model.IdType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.ThreadLocalRandom;
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

	protected StoredDetails storeBinary(
			IBinaryStorageSvc binaryStorageSvc, IdType resourceId, int size, String contentType) throws IOException {
		byte[] payload = randomBytes(size);
		SystemRequestDetails requestDetails = new SystemRequestDetails();
		StoredDetails stored;
		try (ByteArrayInputStream input = new ByteArrayInputStream(payload)) {
			stored =
					binaryStorageSvc.storeBinaryContent(resourceId, null, contentType, input, requestDetails);
		}
		assertThat(binaryStorageSvc.fetchBinaryContent(resourceId, stored.getBinaryContentId()))
				.containsExactly(payload);
		return stored;
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

	private long regularFileCount(Path baseDir) throws IOException {
		if (Files.notExists(baseDir)) {
			return 0;
		}
		try (Stream<Path> files = Files.walk(baseDir)) {
			return files.filter(Files::isRegularFile).count();
		}
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
}

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
		"hapi.fhir.binary_storage_mode=DATABASE"
	}
)
class BinaryStorageDatabaseModeIT extends BaseBinaryStorageIntegrationTest {

	@Autowired
	private IBinaryStorageSvc binaryStorageSvc;

	@Autowired
	private IBinaryStorageEntityDao binaryStorageEntityDao;

	@Autowired
	private PlatformTransactionManager transactionManager;

	@Test
	void databaseModeStoresBinaryContentInDatabase() throws IOException {
		assertThat(binaryStorageSvc).isInstanceOf(DatabaseBinaryContentStorageSvcImpl.class);

		storeAndAssertDatabasePersistence(new IdType("Binary/database-small"), 24);
		storeAndAssertDatabasePersistence(new IdType("Binary/database-large"), 150_000);
	}

	private void storeAndAssertDatabasePersistence(IdType resourceId, int size) throws IOException {
		StoredDetails stored = storeBinary(binaryStorageSvc, resourceId, size, "application/octet-stream");

		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		BinaryStorageEntity entity =
				transactionTemplate.execute(status ->
						binaryStorageEntityDao
								.findByIdAndResourceId(stored.getBinaryContentId(), resourceId.toUnqualifiedVersionless().getValue())
								.orElseThrow());

		assertThat(entity.hasStorageContent()).isTrue();
		assertThat(entity.getStorageContentBin()).hasSize(size);

		binaryStorageSvc.expungeBinaryContent(resourceId, stored.getBinaryContentId());
	}
}

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
		"hapi.fhir.binary_storage_mode=FILESYSTEM",
		"hapi.fhir.binary_storage_filesystem_base_directory=" + BinaryStorageFilesystemDefaultIT.BASE_DIRECTORY
	}
)
class BinaryStorageFilesystemDefaultIT extends BaseBinaryStorageIntegrationTest {
	static final String BASE_DIRECTORY = "target/test-binary-storage/filesystem-default";

	@Autowired
	private FilesystemBinaryStorageSvcImpl filesystemBinaryStorageSvc;

	@AfterEach
	void cleanUp() throws IOException {
		deleteDirectoryContents(baseDir());
	}

	@Test
	void filesystemModeUsesDefaultThresholdForLargePayloads() throws Exception {
		assertThat(filesystemBinaryStorageSvc.getMinimumBinarySize()).isEqualTo(102_400);

		IdType resourceId = new IdType("Binary/filesystem-default");
		String contentType = "application/octet-stream";

		assertThat(filesystemBinaryStorageSvc.shouldStoreBinaryContent(50_000, resourceId, contentType)).isFalse();
		assertRegularFileCount(baseDir(), 0);

		StoredDetails largeStored = storeBinary(filesystemBinaryStorageSvc, resourceId, 150_000, contentType);

		assertRegularFileCountGreaterThan(baseDir(), 0);

		filesystemBinaryStorageSvc.expungeBinaryContent(resourceId, largeStored.getBinaryContentId());
	}

	private Path baseDir() throws IOException {
		Path baseDir = Paths.get(BASE_DIRECTORY).toAbsolutePath();
		Files.createDirectories(baseDir);
		return baseDir;
	}
}

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
		"hapi.fhir.binary_storage_mode=FILESYSTEM",
		"hapi.fhir.binary_storage_filesystem_base_directory=" + BinaryStorageFilesystemCustomThresholdIT.BASE_DIRECTORY,
		"hapi.fhir.inline_resource_storage_below_size=32768"
	}
)
class BinaryStorageFilesystemCustomThresholdIT extends BaseBinaryStorageIntegrationTest {
	static final String BASE_DIRECTORY = "target/test-binary-storage/filesystem-custom";

	@Autowired
	private FilesystemBinaryStorageSvcImpl filesystemBinaryStorageSvc;

	@AfterEach
	void cleanUp() throws IOException {
		deleteDirectoryContents(baseDir());
	}

	@Test
	void filesystemModeHonoursCustomThreshold() throws Exception {
		assertThat(filesystemBinaryStorageSvc.getMinimumBinarySize()).isEqualTo(32_768);

		IdType resourceId = new IdType("Binary/filesystem-custom");
		String contentType = "application/octet-stream";

		assertThat(filesystemBinaryStorageSvc.shouldStoreBinaryContent(30_000, resourceId, contentType)).isFalse();
		assertRegularFileCount(baseDir(), 0);

		StoredDetails stored = storeBinary(filesystemBinaryStorageSvc, resourceId, 40_000, contentType);

		assertRegularFileCountGreaterThan(baseDir(), 0);

		filesystemBinaryStorageSvc.expungeBinaryContent(resourceId, stored.getBinaryContentId());
	}

	private Path baseDir() throws IOException {
		Path baseDir = Paths.get(BASE_DIRECTORY).toAbsolutePath();
		Files.createDirectories(baseDir);
		return baseDir;
	}
}
