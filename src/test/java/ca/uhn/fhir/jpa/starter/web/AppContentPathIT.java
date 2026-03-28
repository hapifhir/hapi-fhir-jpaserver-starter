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
 * Integration test that verifies the app_content_path feature works correctly.
 * 
 * This test validates that:
 * 1. The application starts successfully with app_content_path configured
 * 2. Static files from the app content path are served at /web/**
 * 3. Redirects work correctly for /web and /web/
 * 
 * The app content path "./configs/app" corresponds to the repository's configs/app/ directory
 * which contains sample app content (index.html). This directory is used as a web application
 * root and is served under the /web/** URL pattern.
 * See the application.yaml comment: "# app_content_path: ./configs/app"
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "hapi.fhir.fhir_version=r4",
    "hapi.fhir.app_content_path=./configs/app"  // Uses the repository's configs/app/ directory
})
class AppContentPathIT {
    @Autowired
    ApplicationContext applicationContext;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testApplicationStartsWithAppContentPath() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void testWebIndexHtmlServed() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/web/index.html", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    void testWebRootAccessible() {
        // The /web endpoint is configured to redirect to /web/index.html via the 
        // addViewControllers in WebAppFilesConfigurer. TestRestTemplate follows 
        // redirects automatically, so after the redirect we expect 200 OK.
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/web", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
    
    @Test
    void testWebSlashAccessible() {
        // The /web/ endpoint is configured to redirect to index.html via the
        // addViewControllers in WebAppFilesConfigurer. TestRestTemplate follows
        // redirects automatically, so after the redirect we expect 200 OK.
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/web/", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
