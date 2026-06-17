import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Function;

/**
 * Container healthcheck for the HAPI FHIR JPA Server distroless image.
 *
 * Probes the Spring Boot Actuator health endpoint and exits 0 on a 2xx
 * response, 1 otherwise. See
 * https://github.com/hapifhir/hapi-fhir-jpaserver-starter/issues/958
 *
 * Configuration (read from environment variables, with Spring Boot defaults):
 * SERVER_PORT (default "8080")
 * SERVER_SERVLET_CONTEXT_PATH (default "")
 * MANAGEMENT_SERVER_PORT (default = SERVER_PORT)
 * MANAGEMENT_SERVER_BASE_PATH (default ""; only applies when management has its
 * own port)
 * MANAGEMENT_ENDPOINTS_WEB_BASE_PATH (default "")
 */
public final class HealthCheck {

	private HealthCheck() {}

	public static void main(String[] args) {
		String url = buildHealthUrl(System::getenv);

		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(2))
					.build();
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.timeout(Duration.ofSeconds(3))
					.GET()
					.build();
			int code =
					client.send(request, HttpResponse.BodyHandlers.discarding()).statusCode();
			if (code == 200) {
				System.exit(0);
			}
			System.err.println("Healthcheck failed: HTTP " + code + " from " + url);
			System.exit(1);
		} catch (Exception e) {
			System.err.println("Healthcheck error probing " + url + ": " + e.getMessage());
			System.exit(1);
		}
	}

	static String buildHealthUrl(Function<String, String> env) {
		String serverPort = firstNonBlank(env.apply("SERVER_PORT"), "8080");
		String managementPort = firstNonBlank(env.apply("MANAGEMENT_SERVER_PORT"), serverPort);
		String contextPath = normalize(firstNonBlank(env.apply("SERVER_SERVLET_CONTEXT_PATH"), ""));
		String managementBasePath = normalize(firstNonBlank(env.apply("MANAGEMENT_SERVER_BASE_PATH"), ""));
		String actuatorBasePath =
				normalize(firstNonBlank(env.apply("MANAGEMENT_ENDPOINTS_WEB_BASE_PATH"), "/actuator"));

		// Spring Boot: context-path only applies when management shares the main port;
		// otherwise management.server.base-path is used on the management port.
		String prefix = managementPort.equals(serverPort) ? contextPath : managementBasePath;
		return "http://127.0.0.1:" + managementPort + prefix + actuatorBasePath + "/health";
	}

	private static String firstNonBlank(String... values) {
		for (String v : values) {
			if (v != null && !v.isBlank()) {
				return v;
			}
		}
		return "";
	}

	private static String normalize(String path) {
		if (path == null || path.isBlank() || "/".equals(path)) {
			return "";
		}
		String p = path.startsWith("/") ? path : "/" + path;
		return p.endsWith("/") ? p.substring(0, p.length() - 1) : p;
	}
}
