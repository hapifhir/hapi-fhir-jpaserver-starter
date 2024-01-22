package ch.ahdis.matchbox.gazelle.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.*;

/**
 * matchbox
 *
 * @author Quentin Ligier
 **/
public class ValidationReport {
	private String modelVersion = "0.1";
	private String uuid;
	private Date dateTime;
	private String disclaimer;
	private ValidationMethod validationMethod;
	private ValidationTestResult overallResult = ValidationTestResult.UNDEFINED;
	private List<Metadata> additionalMetadata;
	private List<ValidationSubReport> reports;
	private List<ValidationItem> validationItems;
	private ValidationCounters counters = new ValidationCounters();

	public ValidationReport() {
		this.validationMethod = new ValidationMethod();
		this.dateTime = new Date();
		this.uuid = UUID.randomUUID().toString();
	}

	public String getUuid() {
		return this.uuid;
	}

	public ValidationReport setUuid(String uuid) {
		this.uuid = uuid;
		return this;
	}

	public String getModelVersion() {
		return this.modelVersion;
	}

	public ValidationReport setModelVersion(String modelVersion) {
		this.modelVersion = modelVersion;
		return this;
	}

	public Date getDateTime() {
		return this.dateTime;
	}

	public ValidationReport setDateTime(Date dateTime) {
		this.dateTime = dateTime;
		return this;
	}

	public String getDisclaimer() {
		return this.disclaimer;
	}

	public ValidationReport setDisclaimer(String disclaimer) {
		this.disclaimer = disclaimer;
		return this;
	}

	public ValidationMethod getValidationMethod() {
		return this.validationMethod;
	}

	public ValidationReport setValidationMethod(ValidationMethod validationMethod) {
		this.validationMethod = validationMethod;
		return this;
	}

	public ValidationCounters getCounters() {
		return this.counters;
	}

	public ValidationReport setCounters(ValidationCounters counters) {
		this.counters = counters;
		return this;
	}

	public ValidationTestResult getOverallResult() {
		return this.overallResult;
	}

	public ValidationReport setOverallResult(ValidationTestResult overallResult) {
		this.overallResult = overallResult;
		return this;
	}

	public List<ValidationSubReport> getReports() {
		return this.reports;
	}

	public ValidationReport setReports(List<ValidationSubReport> reports) {
		this.reports = reports;
		return this;
	}

	public List<Metadata> getAdditionalMetadata() {
		return this.additionalMetadata;
	}

	public ValidationReport setAdditionalMetadata(List<Metadata> additionalMetadata) {
		this.additionalMetadata = additionalMetadata;
		return this;
	}

	public ValidationReport addAdditionalMetadata(Metadata metadata) {
		if (this.additionalMetadata == null) {
			this.additionalMetadata = new ArrayList();
		}

		this.additionalMetadata.add(metadata);
		return this;
	}

	public ValidationReport addValidationItem(ValidationItem validationItem) {
		if (this.validationItems == null) {
			this.validationItems = new ArrayList();
		}

		this.validationItems.add(validationItem);
		return this;
	}

	public ValidationReport addValidationSubReport(ValidationSubReport validationSubReport) {
		if (this.reports == null) {
			this.reports = new ArrayList();
		}

		this.reports.add(validationSubReport);
		return this;
	}

	public List<ValidationItem> getValidationItems() {
		return this.validationItems;
	}

	public ValidationReport setValidationItems(List<ValidationItem> validationItems) {
		this.validationItems = validationItems;
		return this;
	}

	public void computeCounters() {
		if (this.reports != null) {
			for(ValidationSubReport report : this.reports) {
				this.counters.setNumberOfAssertions(this.counters.getNumberOfAssertions() + report.getSubCounters().getNumberOfAssertions());
				this.counters.setNumberOfFailedWithInfos(this.counters.getNumberOfFailedWithInfos() + report.getSubCounters().getNumberOfFailedWithInfos());
				this.counters.setNumberOfFailedWithWarnings(this.counters.getNumberOfFailedWithWarnings() + report.getSubCounters().getNumberOfFailedWithWarnings());
				this.counters.setNumberOfFailedWithErrors(this.counters.getNumberOfFailedWithErrors() + report.getSubCounters().getNumberOfFailedWithErrors());
				this.counters.setNumberOfUnexpectedErrors(this.counters.getNumberOfUnexpectedErrors() + report.getSubCounters().getNumberOfUnexpectedErrors());
			}
		}
	}

	public void computeOverallResult() {
		this.overallResult = this.reports != null
			? (ValidationTestResult)this.reports
			.stream()
			.map(ValidationSubReport::getSubReportResult)
			.filter(Objects::nonNull)
			.max(ValidationSubReport::keepHeaviestResult)
			.orElse(ValidationTestResult.UNDEFINED)
			: ValidationTestResult.UNDEFINED;
	}

	@JsonIgnore
	public boolean isDateTimeValid() {
		return this.dateTime != null;
	}

	@JsonIgnore
	public boolean isDisclaimerValid() {
		return this.disclaimer != null && !this.disclaimer.isBlank();
	}

	@JsonIgnore
	public boolean isValidationMethodValid() {
		return this.validationMethod != null;
	}

	@JsonIgnore
	public boolean isValidationCountersValid() {
		ValidationReport validationReport = clone(this);
		return validationReport.getCounters().equals(this.counters);
	}
	@JsonIgnore

	public boolean isValidationTestResultValid() {
		return this.overallResult != null;
	}

	@JsonIgnore
	public boolean isModelVersionValid() {
		return this.modelVersion == null || !this.modelVersion.isBlank();
	}

	@JsonIgnore
	public boolean isReportsValid() {
		return this.reports == null || !this.reports.isEmpty();
	}

	@JsonIgnore
	public boolean isAdditionalMetadataValid() {
		return this.additionalMetadata == null || !this.additionalMetadata.isEmpty();
	}

	@JsonIgnore
	public boolean isValidationItemsValid() {
		return this.validationItems == null || !this.validationItems.isEmpty();
	}

	@JsonIgnore
	public boolean isUuidValid() {
		return this.uuid != null && !this.uuid.isBlank();
	}

	@JsonIgnore
	public boolean isValid() {
		return this.isDateTimeValid()
			&& this.isDisclaimerValid()
			&& this.isValidationMethodValid()
			&& this.isValidationCountersValid()
			&& this.isValidationTestResultValid()
			&& this.isReportsValid()
			&& this.isAdditionalMetadataValid()
			&& this.isValidationItemsValid()
			&& this.isUuidValid();
	}

	static ValidationReport clone(ValidationReport validationReport) {
		return validationReport == null
			? null
			: new ValidationReport()
			.setDisclaimer(validationReport.getDisclaimer())
			.setDateTime(validationReport.getDateTime())
			.setModelVersion(validationReport.getModelVersion())
			.setUuid(validationReport.getUuid())
			.setValidationMethod(ValidationMethod.clone(validationReport.getValidationMethod()))
			.setValidationItems(
				validationReport.getValidationItems() != null ? validationReport.getValidationItems().stream().map(ValidationItem::clone).toList() : null
			)
			.setReports(validationReport.getReports() != null ? validationReport.getReports().stream().map(ValidationSubReport::clone).toList() : null)
			.setAdditionalMetadata(
				validationReport.getAdditionalMetadata() != null ? validationReport.getAdditionalMetadata().stream().map(Metadata::clone).toList() : null
			)
			.setCounters(ValidationCounters.clone(validationReport.getCounters()))
			.setOverallResult(validationReport.getOverallResult());
	}
}
