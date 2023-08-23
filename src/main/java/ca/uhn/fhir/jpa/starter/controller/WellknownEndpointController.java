package ca.uhn.fhir.jpa.starter.controller;

import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.jpa.starter.util.WellknownEndpointHelper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;

@RestController
public class WellknownEndpointController {

    @Autowired
    AppProperties appProperties;

    /**
     * Get request to support well-known endpoints for authorization metadata.
     * See https://www.hl7.org/fhir/smart-app-launch/conformance.html#using-well-known.
     *
     * @return String representing json object of metadata returned at this url
     * @throws IOException when the request fails
     */
    @GetMapping(path = "/smart-configuration", produces = { MediaType.APPLICATION_JSON_VALUE })
    public String getWellKnownJson(HttpServletRequest theRequest) {
      return WellknownEndpointHelper.getWellKnownJson(appProperties);
    }
}
