package ch.ahdis.matchbox.gazelle;

import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationItem;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationProfile;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationReport;
import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class GazelleClient {

	private final URI serverUri;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final HttpClient httpClient;

	public GazelleClient(final String serverUrl) {
		this.serverUri = URI.create(serverUrl);
		this.httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(3))
			.followRedirects(HttpClient.Redirect.NEVER)
			.build();
	}

	public List<ValidationProfile> getProfiles() throws IOException, InterruptedException {
		final HttpRequest request = HttpRequest.newBuilder()
			.uri(this.serverUri.resolve("validation/profiles"))
			.GET()
			.build();

		final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return this.objectMapper.readValue(response.body(), new TypeReference<>() {});
	}

	public ValidationReport validate(final String content, final String profileId) throws IOException, InterruptedException {
		final var validationRequest = new ValidationRequest();
		validationRequest.setValidationServiceName("Matchbox");
		validationRequest.setValidationProfileId(profileId);
		validationRequest.setApiVersion("3.9.9");
		validationRequest.addValidationItem(new ValidationItem()
															.setItemId("first")
															.setContent(content.getBytes(StandardCharsets.UTF_8))
															.setRole("request")
															.setLocation("localhost"));

		final var dest = this.serverUri.resolve("validation/validate");
		System.out.printf("Destination: %s%n", dest);

		final HttpRequest request = HttpRequest.newBuilder(dest)
			.uri(dest)
			.POST(HttpRequest.BodyPublishers.ofString(this.objectMapper.writeValueAsString(validationRequest)))
			.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
			.build();

		final HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return this.objectMapper.readValue(response.body(), new TypeReference<>() {});
	}
}
