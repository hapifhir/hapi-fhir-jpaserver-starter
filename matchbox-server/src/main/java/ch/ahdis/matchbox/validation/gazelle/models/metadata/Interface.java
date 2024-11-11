package ch.ahdis.matchbox.validation.gazelle.models.metadata;

import ch.ahdis.matchbox.validation.gazelle.models.validation.ValidationProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"type", "interfaceName", "interfaceVersion", "required", "bindings"})
public class Interface {

	@JsonProperty("interfaceName")
	private String interfaceName;

	@JsonProperty("interfaceVersion")
	private String interfaceVersion;

	@JsonProperty("required")
	private boolean required;

	@JsonProperty("bindings")
	private List<RestBinding> bindings = new ArrayList<>();

	@JsonProperty("type")
	private String type;

	@JsonProperty("validationProfiles")
	private List<ValidationProfile> validationProfiles = new ArrayList<>();

	public String getInterfaceName() {
		return interfaceName;
	}

	public Interface setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
		return this;
	}

	public String getInterfaceVersion() {
		return interfaceVersion;
	}

	public Interface setInterfaceVersion(String interfaceVersion) {
		this.interfaceVersion = interfaceVersion;
		return this;
	}

	public boolean isRequired() {
		return required;
	}

	public Interface setRequired(boolean required) {
		this.required = required;
		return this;
	}

	public List<RestBinding> getBindings() {
		return bindings;
	}

	public Interface setBindings(List<RestBinding> bindings) {
		this.bindings = bindings;
		return this;
	}

	public Interface addBinding(RestBinding binding) {
		this.bindings.add(binding);
		return this;
	}

	public List<ValidationProfile> getValidationProfiles() {
		return validationProfiles;
	}

	public Interface setValidationProfiles(List<ValidationProfile> validationProfiles) {
		this.validationProfiles = validationProfiles;
		return this;
	}

	public Interface addValidationProfile(ValidationProfile validationProfile) {
		this.validationProfiles.add(validationProfile);
		return this;
	}

	public String getType() {
		return this.type;
	}

	public Interface setType(String type) {
		this.type = type;
		return this;
	}

	@JsonIgnore
	public boolean isInterfaceNameValid(){
		return interfaceName != null && !interfaceName.isBlank();
	}

	@JsonIgnore
	public boolean isInterfaceVersionValid(){
		return interfaceVersion != null && !interfaceVersion.isBlank();
	}

	@JsonIgnore
	public boolean isBindingsValid() {
		return bindings != null && !bindings.isEmpty();
	}

}

