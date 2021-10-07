package ca.uhn.fhir.jpa.starter.smart;

import org.codehaus.jettison.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class WellKnownEndpointController {
	/**
	 * Get request to support well-known endpoints for authorization metadata. See
	 * http://www.hl7.org/fhir/smart-app-launch/conformance/index.html#using-well-known
	 *
	 * @param theRequest Incoming request, unused here
	 * @return String representing json object of metadata returned at this url
	 */
	@GetMapping(path = "/smart-configuration", produces = {"application/json"})
	public String getWellKnownJson(HttpServletRequest theRequest) throws JSONException {
		String yourTokenUrl = ""; // get by configuration here
		String yourRegistrationUrl = ""; // get by configuration here

		return WellknownEndpointHelper.getWellKnownJson(yourTokenUrl, yourRegistrationUrl);
	}



}
