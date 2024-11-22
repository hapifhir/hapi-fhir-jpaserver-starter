package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.fhir.rest.api.server.cdshooks.CdsServiceRequestJson;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"extension"})
public class CdsHooksRequest extends CdsServiceRequestJson {}
