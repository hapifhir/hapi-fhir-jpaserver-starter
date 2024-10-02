package ca.uhn.fhir.jpa.starter.Controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/public/hello")
    public String publicHello() {
        return "Hello, Public!";
    }

    @GetMapping("/secure/hello")
    public String secureHello() {
        return "Hello, Secured World!";
    }
}
