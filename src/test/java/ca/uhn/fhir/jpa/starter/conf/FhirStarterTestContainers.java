package ca.uhn.fhir.jpa.starter.conf;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Paths;
import java.util.Properties;

public class FhirStarterTestContainers {
	public static PostgreSQLContainer<?> postgreSQLContainer = null;
	private static GenericContainer<?> keycloakContainer = null;
	private static Network internalNetwork = null;

	public static GenericContainer<?> getKeycloakContainer() {
		if (keycloakContainer == null) {
			keycloakContainer = new GenericContainer<>(new ImageFromDockerfile()
				.withDockerfile(Paths.get("src/test/resources/Dockerfile")))
				.withNetwork(getNetwork())
				.withExposedPorts(8080)
				.withEnv("DB_ADDR", "postgres-db")
				.withEnv("DB_PORT", "5432")
				.waitingFor(Wait.forHttp("/auth"));
			keycloakContainer.start();
		}
		return keycloakContainer;
	}

	public static PostgreSQLContainer<?> getPostgreSQLContainer() {
		if (postgreSQLContainer == null) {
			postgreSQLContainer = new PostgreSQLContainer<>("postgres:13")
				.withNetwork(getNetwork())
				.withNetworkAliases("postgres-db")
				.withExposedPorts(5432)
				.withDatabaseName("keycloak")
				.withUsername("keycloak")
				.withPassword("keycloak")
				.withCommand("postgres -c max_prepared_transactions=100");
			postgreSQLContainer.start();
		}
		return postgreSQLContainer;
	}

	public static class KeycloakContainerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		// Due to the dynamic nature of the keycloak container ports, we cannot add a static test application.yml file, so instead we add a new property source
		@Override
		public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
				final GenericContainer<?> keycloakContainer = getKeycloakContainer();
				Properties props = new Properties();
				props.put("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", "http://localhost:" + keycloakContainer.getMappedPort(8080)+"/auth/realms/smart/protocol/openid-connect/certs");
				props.put("spring.security.oauth2.resourceserver.jwt.issuer-uri", "http://localhost:" + keycloakContainer.getMappedPort(8080)+"/auth/realms/smart");
				applicationContext.getEnvironment().getPropertySources().addFirst(new PropertiesPropertySource("mockProperties",props));
		}
	}

	private static Network getNetwork(){
		if(internalNetwork == null){
			internalNetwork = Network.newNetwork();
		}
		return internalNetwork;
	}

}
