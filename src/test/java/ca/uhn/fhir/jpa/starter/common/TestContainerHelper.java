package ca.uhn.fhir.jpa.starter.common;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.elasticsearch.ElasticsearchContainer;

/**
 * Helper class for creating and configuring Testcontainers used in integration tests.
 * <p>
 * This class provides factory methods for creating pre-configured containers and utility methods
 * for registering container properties with Spring's {@link DynamicPropertyRegistry}.
 * </p>
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * @Testcontainers
 * @SpringBootTest
 * class MyIntegrationTest {
 *
 *     @Container
 *     private static final PostgreSQLContainer<?> POSTGRES = TestContainerHelper.newPostgresContainer();
 *
 *     @Container
 *     private static final ElasticsearchContainer ELASTICSEARCH = TestContainerHelper.newElasticsearchContainer();
 *
 *     @DynamicPropertySource
 *     static void registerProperties(DynamicPropertyRegistry registry) {
 *         TestContainerHelper.registerPostgresProperties(registry, POSTGRES);
 *         TestContainerHelper.registerElasticsearchProperties(registry, ELASTICSEARCH);
 *     }
 * }
 * }</pre>
 */
public final class TestContainerHelper {

	// Container image versions
	private static final String POSTGRES_IMAGE = "postgres:16-alpine";
	private static final String ELASTICSEARCH_IMAGE = "elasticsearch:8.19.10";

	// Default PostgreSQL configuration
	private static final String DEFAULT_DATABASE_NAME = "hapi";
	private static final String DEFAULT_USERNAME = "fhiruser";
	private static final String DEFAULT_PASSWORD = "fhirpass";

	// Hibernate dialect for HAPI FHIR with PostgreSQL
	private static final String HAPI_POSTGRES_DIALECT = "ca.uhn.fhir.jpa.model.dialect.HapiFhirPostgresDialect";

	private TestContainerHelper() {
		// Utility class - prevent instantiation
	}

	/**
	 * Creates a new PostgreSQL container with default HAPI FHIR configuration.
	 * <p>
	 * The container is configured with:
	 * <ul>
	 *   <li>Image: postgres:16-alpine</li>
	 *   <li>Database name: hapi</li>
	 *   <li>Username: fhiruser</li>
	 *   <li>Password: fhirpass</li>
	 * </ul>
	 *
	 * @return a new pre-configured PostgreSQL container
	 */
	public static PostgreSQLContainer<?> newPostgresContainer() {
		return new PostgreSQLContainer<>(POSTGRES_IMAGE)
			.withDatabaseName(DEFAULT_DATABASE_NAME)
			.withUsername(DEFAULT_USERNAME)
			.withPassword(DEFAULT_PASSWORD);
	}

	/**
	 * Creates a new Elasticsearch container with default configuration for HAPI FHIR.
	 * <p>
	 * The container is configured with:
	 * <ul>
	 *   <li>Image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0</li>
	 *   <li>Security disabled (xpack.security.enabled=false)</li>
	 *   <li>Single-node discovery mode</li>
	 *   <li>JVM heap: 512MB min/max</li>
	 * </ul>
	 *
	 * @return a new pre-configured Elasticsearch container
	 */
	public static ElasticsearchContainer newElasticsearchContainer() {
		return new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
			.withEnv("xpack.security.enabled", "false")
			.withEnv("discovery.type", "single-node")
			.withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
	}

	/**
	 * Registers PostgreSQL container properties with Spring's DynamicPropertyRegistry.
	 * <p>
	 * Registers the following properties:
	 * <ul>
	 *   <li>spring.datasource.url</li>
	 *   <li>spring.datasource.username</li>
	 *   <li>spring.datasource.password</li>
	 *   <li>spring.datasource.driver-class-name</li>
	 *   <li>spring.jpa.properties.hibernate.dialect</li>
	 * </ul>
	 *
	 * @param registry  the Spring dynamic property registry
	 * @param container the PostgreSQL container
	 */
	public static void registerPostgresProperties(DynamicPropertyRegistry registry, PostgreSQLContainer<?> container) {
		registry.add("spring.datasource.url", container::getJdbcUrl);
		registry.add("spring.datasource.username", container::getUsername);
		registry.add("spring.datasource.password", container::getPassword);
		registry.add("spring.datasource.driver-class-name", container::getDriverClassName);
		registry.add("spring.jpa.properties.hibernate.dialect", () -> HAPI_POSTGRES_DIALECT);
	}

	/**
	 * Registers Elasticsearch container properties with Spring's DynamicPropertyRegistry.
	 * <p>
	 * Registers the following properties:
	 * <ul>
	 *   <li>spring.jpa.properties.hibernate.search.backend.hosts</li>
	 *   <li>spring.jpa.properties.hibernate.search.backend.protocol</li>
	 *   <li>spring.jpa.properties.hibernate.search.backend.username</li>
	 *   <li>spring.jpa.properties.hibernate.search.backend.password</li>
	 * </ul>
	 *
	 * @param registry  the Spring dynamic property registry
	 * @param container the Elasticsearch container
	 */
	public static void registerElasticsearchProperties(DynamicPropertyRegistry registry, ElasticsearchContainer container) {
		registry.add("spring.jpa.properties.hibernate.search.backend.hosts", container::getHttpHostAddress);
		registry.add("spring.jpa.properties.hibernate.search.backend.protocol", () -> "http");
		registry.add("spring.jpa.properties.hibernate.search.backend.username", () -> "");
		registry.add("spring.jpa.properties.hibernate.search.backend.password", () -> "");
	}

	/**
	 * Registers both PostgreSQL and Elasticsearch container properties with Spring's DynamicPropertyRegistry.
	 *
	 * @param registry      the Spring dynamic property registry
	 * @param postgres      the PostgreSQL container
	 * @param elasticsearch the Elasticsearch container
	 */
	public static void registerPostgresAndElasticsearchProperties(
		DynamicPropertyRegistry registry,
		PostgreSQLContainer<?> postgres,
		ElasticsearchContainer elasticsearch
	) {
		registerPostgresProperties(registry, postgres);
		registerElasticsearchProperties(registry, elasticsearch);
	}

	/**
	 * Returns the Elasticsearch HTTP URL for a container.
	 * <p>
	 * Example: "http://localhost:49152"
	 *
	 * @param container the Elasticsearch container
	 * @return the full HTTP URL to the Elasticsearch instance
	 */
	public static String getElasticsearchHttpUrl(ElasticsearchContainer container) {
		return "http://" + container.getHost() + ":" + container.getMappedPort(9200);
	}

}
