package ch.ahdis.matchbox.validation.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class AssertionReport {
	private String assertionID;
	private String assertionType;
	private String description;
	private String subjectLocation;
	private String subjectValue;
	private String[] requirementIDs;
	private SeverityLevel severity;
	private RequirementPriority priority = RequirementPriority.MANDATORY;
	private ValidationTestResult result = ValidationTestResult.PASSED;
	private List<UnexpectedError> unexpectedErrors;
	private static final Pattern LINE_COL_PATT = Pattern.compile("line \\d+(, column \\d+)?");
	private static final Pattern XPATH_PATT = Pattern.compile("(/[^/]+)+");

	public AssertionReport() {
		this.severity = SeverityLevel.INFO;
	}

	public String getAssertionID() {
		return this.assertionID;
	}

	public AssertionReport setAssertionID(String assertionID) {
		this.assertionID = assertionID;
		return this;
	}

	public String getAssertionType() {
		return this.assertionType;
	}

	public AssertionReport setAssertionType(String assertionType) {
		this.assertionType = assertionType;
		return this;
	}

	public String getDescription() {
		return this.description;
	}

	public AssertionReport setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getSubjectLocation() {
		return this.subjectLocation;
	}

	public AssertionReport setSubjectLocation(String subjectLocation) {
		this.subjectLocation = subjectLocation;
		return this;
	}

	public String getSubjectValue() {
		return this.subjectValue;
	}

	public AssertionReport setSubjectValue(String subjectValue) {
		this.subjectValue = subjectValue;
		return this;
	}

	public String[] getRequirementIDs() {
		return this.requirementIDs;
	}

	public AssertionReport setRequirementIDs(String[] requirementIDs) {
		this.requirementIDs = requirementIDs;
		return this;
	}

	public SeverityLevel getSeverity() {
		return this.severity;
	}

	public AssertionReport setSeverity(SeverityLevel severity) {
		this.severity = severity;
		return this;
	}

	public RequirementPriority getPriority() {
		return this.priority;
	}

	public AssertionReport setPriority(RequirementPriority priority) {
		this.priority = priority;
		return this;
	}

	public ValidationTestResult getResult() {
		return this.result;
	}

	public AssertionReport setResult(ValidationTestResult result) {
		this.result = result;
		return this;
	}

	public List<UnexpectedError> getUnexpectedErrors() {
		return this.unexpectedErrors;
	}

	public AssertionReport setUnexpectedErrors(List<UnexpectedError> unexpectedErrors) {
		this.unexpectedErrors = unexpectedErrors;
		return this;
	}

	public AssertionReport addUnexpectedError(UnexpectedError unexpectedError) {
		if (this.unexpectedErrors == null) {
			this.unexpectedErrors = new ArrayList();
		}

		this.unexpectedErrors.add(unexpectedError);
		return this;
	}

	@JsonIgnore
	public boolean isDescriptionValid() {
		return this.description != null && !this.description.isBlank();
	}

	@JsonIgnore
	public boolean isPriorityValid() {
		return this.priority != null;
	}

	@JsonIgnore
	public boolean isSeverityValid() {
		return this.severity != null;
	}

	@JsonIgnore
	public boolean isRequirementIDsValid() {
		return this.requirementIDs == null || this.requirementIDs.length > 0;
	}

	@JsonIgnore
	public boolean isAssertionTypeValid() {
		return this.assertionType == null || !this.assertionType.isBlank();
	}

	@JsonIgnore
	public boolean isAssertionIDValid() {
		return this.assertionID == null || !this.assertionID.isBlank();
	}

	@JsonIgnore
	public boolean isUnexpectedErrorsValid() {
		return this.unexpectedErrors == null || !this.unexpectedErrors.isEmpty();
	}

	@JsonIgnore
	public boolean isSubjectValueValid() {
		return this.subjectValue == null || !this.subjectValue.isBlank();
	}

	@JsonIgnore
	public boolean isSubjectLocationValid() {
		if (this.subjectLocation == null) {
			return true;
		} else if (this.subjectLocation.isBlank()) {
			return false;
		} else {
			return LINE_COL_PATT.matcher(this.subjectLocation).find() || XPATH_PATT.matcher(this.subjectLocation).find();
		}
	}

	@JsonIgnore
	public boolean isResultValid() {
		return this.result != null;
	}

	@JsonIgnore
	public boolean isValid() {
		return this.isDescriptionValid()
			&& this.isPriorityValid()
			&& this.isSeverityValid()
			&& this.isRequirementIDsValid()
			&& this.isAssertionTypeValid()
			&& this.isAssertionIDValid()
			&& this.isSubjectValueValid()
			&& this.isSubjectLocationValid()
			&& this.isResultValid();
	}

	public void computeSeverity() {
		if (ValidationTestResult.PASSED.equals(this.result)) {
			this.severity = SeverityLevel.INFO;
		} else {
			switch(this.priority) {
				case MANDATORY:
					this.severity = SeverityLevel.ERROR;
					break;
				case RECOMMENDED:
					this.severity = SeverityLevel.WARNING;
					break;
				default:
					this.severity = SeverityLevel.INFO;
			}
		}
	}

	static AssertionReport clone(AssertionReport assertionReport) {
		return assertionReport == null
			? null
			: new AssertionReport()
			.setAssertionID(assertionReport.getAssertionID())
			.setResult(assertionReport.getResult())
			.setSeverity(assertionReport.getSeverity())
			.setPriority(assertionReport.getPriority())
			.setAssertionType(assertionReport.getAssertionType())
			.setSubjectLocation(assertionReport.getSubjectLocation())
			.setRequirementIDs(new String[]{Arrays.toString(assertionReport.getRequirementIDs())})
			.setUnexpectedErrors(
				assertionReport.getUnexpectedErrors() != null ? assertionReport.getUnexpectedErrors().stream().map(UnexpectedError::clone).toList() : null
			);
	}
}
