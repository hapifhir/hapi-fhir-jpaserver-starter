import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

public class HealthCheck {

	public static void main(String[] args) {
		try {
			var port = System.getenv().getOrDefault("SERVER_PORT", "8080");
			var url = new URL("http://localhost:" + port + "/fhir/metadata");
			var conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/fhir+json");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);

			var status = conn.getResponseCode();
			if (status != 200) {
				System.err.println("Health check failed: HTTP " + status);
				System.exit(1);
			}

			var body = new StringBuilder();
			try (var reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					body.append(line);
				}
			}

			var pattern = Pattern.compile("\"resourceType\"\\s*:\\s*\"CapabilityStatement\"");
			if (pattern.matcher(body.toString()).find()) {
				System.exit(0);
			} else {
				System.err.println("Health check failed: CapabilityStatement not found in response");
				System.exit(1);
			}
		} catch (Exception e) {
			System.err.println("Health check failed: " + e.getMessage());
			System.exit(1);
		}
	}
}
