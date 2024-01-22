package ch.ahdis.matchbox.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

/**
 * The model of a validation request, containing a list of items to validate.
 * <p>
 * Copy-pasted from
 * https://gitlab.inria.fr/gazelle/library/validation-service-api/-/blob/master/validation-api/src/main/java/net/ihe/gazelle/validation/api/domain/request/structure/ValidationRequest.java?ref_type=heads
 *
 * @author Achraf Achkari
 * @author Quentin Ligier
 **/
@JsonRootName(value = "validationRequest")
public class ValidationRequest {
	public static final String API_VERSION = "0.1";

	@JsonProperty(value = "apiVersion")
	private String apiVersion = API_VERSION;

	@JsonProperty(value = "validationServiceName")
	private String validationServiceName; // given In URL

	@JsonProperty(value = "validationProfileId")
	private String validationProfileId;

	@JsonProperty(value = "validationItems")
	private List<ValidationItem> validationItems;

	public String getApiVersion() {
		return apiVersion;
	}

	public ValidationRequest setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
		return this;
	}

	public String getValidationServiceName() {
		return validationServiceName;
	}

	public ValidationRequest setValidationServiceName(String validationServiceName) {
		this.validationServiceName = validationServiceName;
		return this;
	}

	public String getValidationProfileId() {
		return validationProfileId;
	}

	public ValidationRequest setValidationProfileId(String validationProfileId) {
		this.validationProfileId = validationProfileId;
		return this;
	}

	public List<ValidationItem> getValidationItems() {
		return validationItems;
	}

	public ValidationRequest setValidationItems(List<ValidationItem> validationItems) {
		this.validationItems = validationItems;
		return this;
	}
	public ValidationRequest addValidationItem(ValidationItem validationItem){
		if(this.validationItems == null){
			this.validationItems = new ArrayList<>();
		}
		this.validationItems.add(validationItem);
		return this;
	}

	public boolean isValidationProfileIdValid(){
		return validationProfileId != null && !validationProfileId.isBlank();
	}

	public boolean isValidationServiceNameValid(){
		return validationServiceName != null && !validationServiceName.isBlank();
	}

	public boolean isValidationItemsValid(){
		return validationItems != null && !validationItems.isEmpty();
	}

	public boolean isValidationItemsRolesValid(){
		return validationItems == null || validationItems.size()<2
			|| validationItems.stream().allMatch(item ->item.getRole() !=null && !item.getRole().isBlank());
	}

	public boolean isValid(){
		return isValidationItemsValid() && isValidationProfileIdValid() && isValidationServiceNameValid();
	}
}
