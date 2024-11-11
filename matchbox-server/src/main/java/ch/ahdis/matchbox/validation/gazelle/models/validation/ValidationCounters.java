package ch.ahdis.matchbox.validation.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class ValidationCounters {
	private Integer numberOfAssertions = 0;
	private Integer numberOfFailedWithInfos = 0;
	private Integer numberOfFailedWithWarnings = 0;
	private Integer numberOfFailedWithErrors = 0;
	private Integer numberOfUnexpectedErrors = 0;

	public ValidationCounters() {
	}

	public Integer getNumberOfUnexpectedErrors() {
		return this.numberOfUnexpectedErrors;
	}

	public ValidationCounters setNumberOfUnexpectedErrors(Integer numberOfUnexpectedErrors) {
		this.numberOfUnexpectedErrors = numberOfUnexpectedErrors;
		return this;
	}

	public Integer getNumberOfAssertions() {
		return this.numberOfAssertions;
	}

	public ValidationCounters setNumberOfAssertions(Integer numberOfAssertions) {
		this.numberOfAssertions = numberOfAssertions;
		return this;
	}

	public Integer getNumberOfFailedWithInfos() {
		return this.numberOfFailedWithInfos;
	}

	public ValidationCounters setNumberOfFailedWithInfos(Integer numberOfFailedWithInfos) {
		this.numberOfFailedWithInfos = numberOfFailedWithInfos;
		return this;
	}

	public Integer getNumberOfFailedWithWarnings() {
		return this.numberOfFailedWithWarnings;
	}

	public ValidationCounters setNumberOfFailedWithWarnings(Integer numberOfFailedWithWarnings) {
		this.numberOfFailedWithWarnings = numberOfFailedWithWarnings;
		return this;
	}

	public Integer getNumberOfFailedWithErrors() {
		return this.numberOfFailedWithErrors;
	}

	public ValidationCounters setNumberOfFailedWithErrors(Integer numberOfFailedWithErrors) {
		this.numberOfFailedWithErrors = numberOfFailedWithErrors;
		return this;
	}

	public void incrementNumberOfAssertions() {
		Integer var1 = this.numberOfAssertions;
		this.numberOfAssertions = this.numberOfAssertions + 1;
	}

	public void incrementFailedWithInfos() {
		Integer var1 = this.numberOfFailedWithInfos;
		this.numberOfFailedWithInfos = this.numberOfFailedWithInfos + 1;
	}

	public void incrementFailedWithWarnings() {
		Integer var1 = this.numberOfFailedWithWarnings;
		this.numberOfFailedWithWarnings = this.numberOfFailedWithWarnings + 1;
	}

	public void incrementFailedWithErrors() {
		Integer var1 = this.numberOfFailedWithErrors;
		this.numberOfFailedWithErrors = this.numberOfFailedWithErrors + 1;
	}

	public void incrementUnexpectedErrors() {
		Integer var1 = this.numberOfUnexpectedErrors;
		this.numberOfUnexpectedErrors = this.numberOfUnexpectedErrors + 1;
	}

	public void addNumbersFromSubCounters(ValidationCounters subCounters) {
		this.numberOfAssertions = this.numberOfAssertions + subCounters.getNumberOfAssertions();
		this.numberOfFailedWithInfos = this.numberOfFailedWithInfos + subCounters.getNumberOfFailedWithInfos();
		this.numberOfFailedWithWarnings = this.numberOfFailedWithWarnings + subCounters.getNumberOfFailedWithWarnings();
		this.numberOfFailedWithErrors = this.numberOfFailedWithErrors + subCounters.getNumberOfFailedWithErrors();
		this.numberOfUnexpectedErrors = this.numberOfUnexpectedErrors + subCounters.getNumberOfUnexpectedErrors();
	}

	@JsonIgnore
	public boolean isNumberOfAssertionsValid() {
		return this.numberOfAssertions != null && this.numberOfAssertions >= 0;
	}

	@JsonIgnore
	public boolean isNumberOfFailedWithInfosValid() {
		return this.numberOfFailedWithInfos != null && this.numberOfFailedWithInfos >= 0;
	}

	@JsonIgnore
	public boolean isNumberOfWarningsValid() {
		return this.numberOfFailedWithWarnings != null && this.numberOfFailedWithWarnings >= 0;
	}

	@JsonIgnore
	public boolean isNumberOfErrorsValid() {
		return this.numberOfFailedWithErrors != null && this.numberOfFailedWithErrors >= 0;
	}

	@JsonIgnore
	public boolean isNumberOfUnexpectedErrorsValid() {
		return this.numberOfUnexpectedErrors != null && this.numberOfUnexpectedErrors >= 0;
	}

	@JsonIgnore
	public boolean isValid() {
		return this.isNumberOfAssertionsValid()
			&& this.isNumberOfFailedWithInfosValid()
			&& this.isNumberOfWarningsValid()
			&& this.isNumberOfErrorsValid()
			&& this.isNumberOfUnexpectedErrorsValid();
	}

	public boolean equals(Object o) {
		if (this == o) {
			return true;
		} else if (!(o instanceof ValidationCounters)) {
			return false;
		} else {
			ValidationCounters that = (ValidationCounters)o;
			return Objects.equals(this.numberOfAssertions, that.numberOfAssertions)
				&& Objects.equals(this.numberOfFailedWithInfos, that.numberOfFailedWithInfos)
				&& Objects.equals(this.numberOfFailedWithWarnings, that.numberOfFailedWithWarnings)
				&& Objects.equals(this.numberOfFailedWithErrors, that.numberOfFailedWithErrors)
				&& Objects.equals(this.numberOfUnexpectedErrors, that.numberOfUnexpectedErrors);
		}
	}

	public int hashCode() {
		return Objects.hash(
			new Object[]{
				this.numberOfAssertions, this.numberOfFailedWithInfos, this.numberOfFailedWithWarnings, this.numberOfFailedWithErrors, this.numberOfUnexpectedErrors
			}
		);
	}

	static ValidationCounters clone(ValidationCounters validationCounters) {
		return validationCounters == null
			? null
			: new ValidationCounters()
			.setNumberOfAssertions(validationCounters.getNumberOfAssertions())
			.setNumberOfFailedWithInfos(validationCounters.getNumberOfFailedWithInfos())
			.setNumberOfFailedWithWarnings(validationCounters.getNumberOfFailedWithWarnings())
			.setNumberOfFailedWithErrors(validationCounters.getNumberOfFailedWithErrors())
			.setNumberOfUnexpectedErrors(validationCounters.getNumberOfUnexpectedErrors());
	}
}
