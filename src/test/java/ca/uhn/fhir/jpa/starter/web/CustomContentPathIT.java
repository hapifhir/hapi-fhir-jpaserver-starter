package ca.uhn.fhir.jpa.starter.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test that verifies the custom_content_path feature works correctly.
 * 
 * This test validates that:
 * 1. The application starts successfully with custom_content_path configured
 * 2. Static files from the custom content path are served at /content/**
 * 3. Files are accessible and contain expected content
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "hapi.fhir.fhir_version=r4",
    "hapi.fhir.custom_content_path=./custom"
})
class CustomContentPathIT {
    @Autowired
    ApplicationContext applicationContext;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testApplicationStartsWithCustomContentPath() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testCustomAboutHtmlServed() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/content/about.html", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("custom about page");
    }
    
    @Test
    void testCustomWelcomeHtmlServed() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/content/welcome.html", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    void testCustomLogoServed() {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/content/logo.jpg", byte[].class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().length).isGreaterThan(0);
    }
}
