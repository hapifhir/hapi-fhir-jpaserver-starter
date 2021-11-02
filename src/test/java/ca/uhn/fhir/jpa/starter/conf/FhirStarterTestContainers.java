package ca.uhn.fhir.jpa.starter.conf;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.nio.file.Paths;

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

	private static Network getNetwork(){
		if(internalNetwork == null){
			internalNetwork = Network.newNetwork();
		}
		return internalNetwork;
	}

}
