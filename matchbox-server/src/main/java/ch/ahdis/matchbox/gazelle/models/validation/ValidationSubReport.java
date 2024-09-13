package ch.ahdis.matchbox.gazelle.models.validation;

import ch.ahdis.matchbox.gazelle.utils.BidMap;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class ValidationSubReport {
	private String name;
	private String[] standards;
	private ValidationTestResult subReportResult = ValidationTestResult.UNDEFINED;
	private List<ValidationSubReport> subReports;
	private List<AssertionReport> assertionReports;
	private ValidationCounters subCounters = new ValidationCounters();
	private List<UnexpectedError> unexpectedErrors;
	private static final BidMap<ValidationTestResult, Integer> validationTestResultWeights = BidMap.of(
            new BidMap.Entry(ValidationTestResult.PASSED, 0),
				new BidMap.Entry(ValidationTestResult.FAILED, 1),
				new BidMap.Entry(ValidationTestResult.UNDEFINED, 2));

	public ValidationSubReport() {
	}

	public String getName() {
		return this.name;
	}

	public ValidationSubReport setName(String name) {
		this.name = name;
		return this;
	}

	public String[] getStandards() {
		return this.standards;
	}

	public ValidationSubReport setStandards(String[] standards) {
		this.standards = standards;
		return this;
	}

	public ValidationTestResult getSubReportResult() {
		return this.subReportResult;
	}

	public ValidationSubReport setSubReportResult(ValidationTestResult subReportResult) {
		this.subReportResult = subReportResult;
		return this;
	}

	public List<ValidationSubReport> getSubReports() {
		return this.subReports;
	}

	public ValidationSubReport setSubReports(List<ValidationSubReport> subReports) {
		this.subReports = subReports;
		return this;
	}

	public List<UnexpectedError> getUnexpectedErrors() {
		return this.unexpectedErrors;
	}

	public ValidationSubReport setUnexpectedErrors(List<UnexpectedError> unexpectedErrors) {
		this.unexpectedErrors = unexpectedErrors;
		return this;
	}

	public List<AssertionReport> getAssertionReports() {
		return this.assertionReports;
	}

	public ValidationSubReport setAssertionReports(List<AssertionReport> assertionReports) {
		this.assertionReports = assertionReports;
		return this;
	}

	public ValidationCounters getSubCounters() {
		return this.subCounters;
	}

	public ValidationSubReport setSubCounters(ValidationCounters subCounters) {
		this.subCounters = subCounters;
		return this;
	}

	public ValidationSubReport addSubReport(ValidationSubReport subReport) {
		if (this.subReports == null) {
			this.subReports = new ArrayList<>();
		}

		this.subReports.add(subReport);
		return this;
	}

	public ValidationSubReport addAssertionReport(AssertionReport assertionReport) {
		if (this.assertionReports == null) {
			this.assertionReports = new ArrayList<>();
		}

		this.assertionReports.add(assertionReport);
		return this;
	}

	public ValidationSubReport addUnexpectedError(UnexpectedError unexpectedError) {
		if (this.unexpectedErrors == null) {
			this.unexpectedErrors = new ArrayList<>();
		}

		this.unexpectedErrors.add(unexpectedError);
		return this;
	}

	@JsonIgnore
	public boolean isNameValid() {
		return this.name != null && !this.name.isBlank();
	}

	@JsonIgnore
	public boolean isSubReportResultValid() {
		return this.subReportResult != null;
	}

	@JsonIgnore
	public boolean isUnexpectedErrorsValid() {
		return this.unexpectedErrors == null || !this.unexpectedErrors.isEmpty();
	}

	@JsonIgnore
	public boolean isAssertionReportsValid() {
		return this.assertionReports == null || !this.assertionReports.isEmpty();
	}

	@JsonIgnore
	public boolean isStandardsValid() {
		return this.standards == null || this.standards.length > 0;
	}

	@JsonIgnore
	public boolean isSubCounterValid() {
		ValidationSubReport temp = clone(this);
		return this.subCounters != null && this.subCounters.equals(temp.getSubCounters());
	}

	@JsonIgnore
	public boolean isSubReportsValid() {
		return this.subReports == null || !this.subReports.isEmpty();
	}

	@JsonIgnore
	public boolean isValid() {
		return this.isNameValid()
			&& this.isSubReportResultValid()
			&& this.isUnexpectedErrorsValid()
			&& this.isAssertionReportsValid()
			&& this.isSubReportsValid()
			&& this.isStandardsValid()
			&& this.isSubCounterValid();
	}

	public void computeResultSubReport() {
		ValidationTestResult assertionsResult = this.assertionReports != null
			? (ValidationTestResult)this.assertionReports
			.stream()
			.map(this::getAssertionResultBySeverity)
			.filter(Objects::nonNull)
			.max(ValidationSubReport::keepHeaviestResult)
			.orElse(ValidationTestResult.PASSED)
			: ValidationTestResult.PASSED;
		// If no assertion report is present, the result is PASSED
		ValidationTestResult subReportsResult = this.subReports != null
			? (ValidationTestResult)this.subReports
			.stream()
			.map(ValidationSubReport::getSubReportResult)
			.filter(Objects::nonNull)
			.max(ValidationSubReport::keepHeaviestResult)
			.orElse(assertionsResult)
			: assertionsResult;
		this.subReportResult = this.getHeaviestResult(assertionsResult, subReportsResult);
	}

	static int keepHeaviestResult(ValidationTestResult result1, ValidationTestResult result2) {
		return validationTestResultWeights.get(result1) - validationTestResultWeights.get(result2);
	}

	private ValidationTestResult getHeaviestResult(ValidationTestResult result1, ValidationTestResult result2) {
		return keepHeaviestResult(result1, result2) > 0 ? result1 : result2;
	}

	private ValidationTestResult getAssertionResultBySeverity(AssertionReport assertionReport) {
		return assertionReport.getResult() == ValidationTestResult.FAILED && assertionReport.getSeverity() != SeverityLevel.ERROR
			? ValidationTestResult.PASSED
			: assertionReport.getResult();
	}

	public void computeCountersSubReport() {
		this.computeCountersFromAssertionReports();
		this.computeCountersFromSubReports();
	}

	private void computeCountersFromAssertionReports() {
		if (this.getAssertionReports() != null) {
			this.getAssertionReports().forEach(this::processAssertionReport);
		}
	}

	private void processAssertionReport(AssertionReport assertionReport) {
		this.getSubCounters().incrementNumberOfAssertions();
		if (assertionReport.getResult() == ValidationTestResult.FAILED) {
			this.processFailedAssertionReport(assertionReport);
		} else if (assertionReport.getResult() == ValidationTestResult.UNDEFINED && assertionReport.getSeverity() == SeverityLevel.ERROR) {
			this.getSubCounters().incrementUnexpectedErrors();
		}
	}

	private void processFailedAssertionReport(AssertionReport assertionReport) {
		if (assertionReport.getSeverity() == SeverityLevel.ERROR) {
			this.getSubCounters().incrementFailedWithErrors();
		} else if (assertionReport.getSeverity() == SeverityLevel.WARNING) {
			this.getSubCounters().incrementFailedWithWarnings();
		} else {
			this.getSubCounters().incrementFailedWithInfos();
		}
	}

	private void computeCountersFromSubReports() {
		if (this.getSubReports() != null) {
			this.getSubReports().forEach(subReport -> {
				subReport.computeCountersSubReport();
				this.getSubCounters().addNumbersFromSubCounters(subReport.getSubCounters());
			});
		}
	}

	static ValidationSubReport clone(ValidationSubReport validationSubReport) {
		if (validationSubReport == null) {
			return null;
		} else {
			String[] standardsClone;
			if (validationSubReport.getStandards() == null) {
				standardsClone = null;
			} else {
				standardsClone = new String[validationSubReport.getStandards().length];
				System.arraycopy(validationSubReport.getStandards(), 0, standardsClone, 0, standardsClone.length);
			}

			return new ValidationSubReport()
				.setName(validationSubReport.getName())
				.setStandards(standardsClone)
				.setSubReportResult(validationSubReport.getSubReportResult())
				.setSubCounters(ValidationCounters.clone(validationSubReport.getSubCounters()))
				.setSubReports(validationSubReport.getSubReports() != null ? validationSubReport.getSubReports().stream().map(v -> clone(v)).toList() : null)
				.setAssertionReports(
					validationSubReport.getAssertionReports() != null ? validationSubReport.getAssertionReports().stream().map(AssertionReport::clone).toList() : null
				)
				.setUnexpectedErrors(
					validationSubReport.getUnexpectedErrors() != null ? validationSubReport.getUnexpectedErrors().stream().map(UnexpectedError::clone).toList() : null
				);
		}
	}
}
