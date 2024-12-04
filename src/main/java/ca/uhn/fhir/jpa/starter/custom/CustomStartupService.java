package ca.uhn.fhir.jpa.starter.custom;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import jakarta.annotation.PostConstruct;

@Service
public class CustomStartupService {

    public CustomStartupService() {
    }

    @PostConstruct
    public void onStartup() {
        fetchJWTCertificate();
    }

    public void fetchJWTCertificate() {
        RestTemplate restTemplate = createRestTemplate();
        String url = CustomEnvironment.auth_url + CustomEnvironment.auth_cert_endpoint;
        String jwtCert = null;

        try {
            System.out.println("\n=====> Custom REST Request <=====");
            System.out.println("\n- URL : " + url);
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            System.out.println("- Status Code : " + response.getStatusCode());
            System.out.println("- Body : \n" + response.getBody() + "\n\n");
            jwtCert = (String) response.getBody().get("cert");
        } catch (Exception e) {
            e.printStackTrace();
        }

        CustomEnvironment.JWT_CERT = jwtCert;
    }

    public static RestTemplate createRestTemplate() {
        int connectionTimeout = 30000; // 30 seconds
        int readTimeout = 30000; // 30 seconds

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }
}
