package ch.ahdis.matchbox.validation.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class ValidationMethod {
	private String validationServiceName;
	private String validationServiceVersion;
	private String validationProfileID;
	private String validationProfileVersion;

	public ValidationMethod() {
	}

	public String getValidationServiceName() {
		return this.validationServiceName;
	}

	public ValidationMethod setValidationServiceName(String validationServiceName) {
		this.validationServiceName = validationServiceName;
		return this;
	}

	public String getValidationServiceVersion() {
		return this.validationServiceVersion;
	}

	public ValidationMethod setValidationServiceVersion(String validationServiceVersion) {
		this.validationServiceVersion = validationServiceVersion;
		return this;
	}

	public String getValidationProfileID() {
		return this.validationProfileID;
	}

	public ValidationMethod setValidationProfileID(String validationProfileID) {
		this.validationProfileID = validationProfileID;
		return this;
	}

	public String getValidationProfileVersion() {
		return this.validationProfileVersion;
	}

	public ValidationMethod setValidationProfileVersion(String validationProfileVersion) {
		this.validationProfileVersion = validationProfileVersion;
		return this;
	}

	@JsonIgnore
	public boolean isValidationServiceNameValid() {
		return this.validationServiceName != null && !this.validationServiceName.isBlank();
	}

	@JsonIgnore
	public boolean isValidationServiceVersionValid() {
		return this.validationServiceVersion != null && !this.validationServiceVersion.isBlank();
	}

	@JsonIgnore
	public boolean isValidationProfileIDValid() {
		return this.validationProfileID != null && !this.validationProfileID.isBlank();
	}

	@JsonIgnore
	public boolean isValidationProfileVersionValid() {
		return this.validationProfileVersion != null && !this.validationProfileVersion.isBlank();
	}

	@JsonIgnore
	public boolean isValid() {
		return this.isValidationServiceNameValid()
			&& this.isValidationServiceVersionValid()
			&& this.isValidationProfileIDValid()
			&& this.isValidationProfileVersionValid();
	}

	static ValidationMethod clone(ValidationMethod validationMethod) {
		return validationMethod == null
			? null
			: new ValidationMethod()
			.setValidationServiceName(validationMethod.getValidationServiceName())
			.setValidationServiceVersion(validationMethod.getValidationServiceVersion())
			.setValidationProfileID(validationMethod.getValidationProfileID())
			.setValidationProfileVersion(validationMethod.getValidationProfileVersion());
	}
}
