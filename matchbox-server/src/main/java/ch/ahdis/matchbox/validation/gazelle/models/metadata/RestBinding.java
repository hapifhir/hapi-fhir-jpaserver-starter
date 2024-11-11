package ch.ahdis.matchbox.validation.gazelle.models.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
@JsonPropertyOrder({"type","serviceUrl"})
public class RestBinding {

	@JsonProperty("type")
	private String type;

	@JsonProperty("serviceUrl")
	private String serviceUrl;

	public String getType() {
		return type;
	}

	public RestBinding setType(String type) {
		this.type = type;
		return this;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public RestBinding setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
		return this;
	}

	@JsonIgnore
	public boolean isServiceUrlValid(){
		return serviceUrl != null && !serviceUrl.isBlank();
	}
}
