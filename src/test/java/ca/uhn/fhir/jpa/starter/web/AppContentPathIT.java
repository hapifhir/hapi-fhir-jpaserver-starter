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
 */
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
    "hapi.fhir.fhir_version=r4",
    "hapi.fhir.app_content_path=./configs/app"
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
        // The /web endpoint should redirect to index.html or serve it directly
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/web", String.class);
        
        // TestRestTemplate follows redirects, so we expect 200 OK after redirect
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.FOUND, HttpStatus.SEE_OTHER);
    }
    
    @Test
    void testWebSlashAccessible() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/web/", String.class);
        
        // Should redirect to index.html
        assertThat(response.getStatusCode()).isIn(HttpStatus.OK, HttpStatus.FOUND, HttpStatus.SEE_OTHER);
    }
}
