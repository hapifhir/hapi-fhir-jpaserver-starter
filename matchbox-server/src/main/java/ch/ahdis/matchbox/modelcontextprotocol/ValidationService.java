package ch.ahdis.matchbox.modelcontextprotocol;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

    @Tool(name = "get_validation", description = "Validate a FHIR resource against a profile")
    public String getValidation(String resource, String profile) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("https://test.ahdis.ch/matchboxv3/fhir/$validate?profile=" + profile + "&analyzeOutcomeWithAiOnError=false"))
                    .POST(BodyPublishers.ofString(resource))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            } else {
                throw new RuntimeException("Failed to validate resource: " + response.statusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Validation failed";
    }
    
}
