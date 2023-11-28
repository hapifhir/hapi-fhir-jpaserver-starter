package ca.uhn.fhir.jpa.starter.cdshooks;

import ca.uhn.hapi.fhir.cdshooks.api.json.CdsServiceRequestJson;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"extension"})
public class CdsHooksRequest extends CdsServiceRequestJson {}
