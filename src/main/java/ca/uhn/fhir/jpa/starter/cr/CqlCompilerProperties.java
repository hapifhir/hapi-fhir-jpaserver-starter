package ca.uhn.fhir.jpa.starter.cr;

import org.cqframework.cql.cql2elm.CqlCompilerException;
import org.cqframework.cql.cql2elm.CqlTranslator;
import org.cqframework.cql.cql2elm.LibraryBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hapi.fhir.cr.cql.compiler")
public class CqlCompilerProperties {
	private Boolean validate_units = true;
	private Boolean verify_only = false;
	private String compatibility_level = "1.5";
	private CqlCompilerException.ErrorSeverity error_level = CqlCompilerException.ErrorSeverity.Info;
	private LibraryBuilder.SignatureLevel signature_level = LibraryBuilder.SignatureLevel.All;
	private Boolean analyze_data_requirements = false;
	private Boolean collapse_data_requirements = false;
	private CqlTranslator.Format translator_format = CqlTranslator.Format.JSON;
	private Boolean enable_date_range_optimization = true;
	private Boolean enable_annotations = true;
	private Boolean enable_locators = true;
	private Boolean enable_results_type = true;
	private Boolean enable_detailed_errors = true;
	private Boolean disable_list_traversal = false;
	private Boolean disable_list_demotion = false;
	private Boolean disable_list_promotion = false;
	private Boolean enable_interval_demotion = false;
	private Boolean enable_interval_promotion = false;
	private Boolean disable_method_invocation = false;
	private Boolean require_from_keyword = false;
	private Boolean disable_default_model_info_load = false;

	public boolean isValidateUnits() {
		return validate_units;
	}

	public void setValidateUnits(boolean validateUnits) {
		this.validate_units = validateUnits;
	}

	public boolean isVerifyOnly() {
		return verify_only;
	}

	public void setVerifyOnly(boolean verifyOnly) {
		this.verify_only = verifyOnly;
	}

	public String getCompatibilityLevel() {
		return compatibility_level;
	}

	public void setCompatibilityLevel(String compatibilityLevel) {
		this.compatibility_level = compatibilityLevel;
	}

	public CqlCompilerException.ErrorSeverity getErrorSeverityLevel() {
		return error_level;
	}

	public void setErrorSeverityLevel(CqlCompilerException.ErrorSeverity errorSeverityLevel) {
		this.error_level = errorSeverityLevel;
	}

	public LibraryBuilder.SignatureLevel getSignatureLevel() {
		return signature_level;
	}

	public void setSignatureLevel(LibraryBuilder.SignatureLevel signatureLevel) {
		this.signature_level = signatureLevel;
	}

	public boolean isAnalyzeDataRequirements() {
		return analyze_data_requirements;
	}

	public void setAnalyzeDataRequirements(boolean analyzeDataRequirements) {
		this.analyze_data_requirements = analyzeDataRequirements;
	}

	public boolean isCollapseDataRequirements() {
		return collapse_data_requirements;
	}

	public void setCollapseDataRequirements(boolean collapseDataRequirements) {
		this.collapse_data_requirements = collapseDataRequirements;
	}

	public boolean isEnableDateRangeOptimization() {
		return enable_date_range_optimization;
	}

	public void setEnableDateRangeOptimization(boolean enableDateRangeOptimization) {
		this.enable_date_range_optimization = enableDateRangeOptimization;
	}

	public boolean isEnableAnnotations() {
		return enable_annotations;
	}

	public void setEnableAnnotations(boolean enableAnnotations) {
		this.enable_annotations = enableAnnotations;
	}

	public boolean isEnableLocators() {
		return enable_locators;
	}

	public void setEnableLocators(boolean enableLocators) {
		this.enable_locators = enableLocators;
	}

	public boolean isEnableResultsType() {
		return enable_results_type;
	}

	public void setEnableResultsType(boolean enableResultsType) {
		this.enable_results_type = enableResultsType;
	}

	public boolean isEnableDetailedErrors() {
		return enable_detailed_errors;
	}

	public void setEnableDetailedErrors(boolean enableDetailedErrors) {
		this.enable_detailed_errors = enableDetailedErrors;
	}

	public boolean isDisableListTraversal() {
		return disable_list_traversal;
	}

	public void setDisableListTraversal(boolean disableListTraversal) {
		this.disable_list_traversal = disableListTraversal;
	}

	public boolean isDisableListDemotion() {
		return disable_list_demotion;
	}

	public void setDisableListDemotion(boolean disableListDemotion) {
		this.disable_list_demotion = disableListDemotion;
	}

	public boolean isDisableListPromotion() {
		return disable_list_promotion;
	}

	public void setDisableListPromotion(boolean disableListPromotion) {
		this.disable_list_promotion = disableListPromotion;
	}

	public boolean isEnableIntervalPromotion() {
		return enable_interval_promotion;
	}

	public void setEnableIntervalPromotion(boolean enableIntervalPromotion) {
		this.enable_interval_promotion = enableIntervalPromotion;
	}

	public boolean isEnableIntervalDemotion() {
		return enable_interval_demotion;
	}

	public void setEnableIntervalDemotion(boolean enableIntervalDemotion) {
		this.enable_interval_demotion = enableIntervalDemotion;
	}

	public boolean isDisableMethodInvocation() {
		return disable_method_invocation;
	}

	public void setDisableMethodInvocation(boolean disableMethodInvocation) {
		this.disable_method_invocation = disableMethodInvocation;
	}

	public boolean isRequireFromKeyword() {
		return require_from_keyword;
	}

	public void setRequireFromKeyword(boolean requireFromKeyword) {
		this.require_from_keyword = requireFromKeyword;
	}

	public boolean isDisableDefaultModelInfoLoad() {
		return disable_default_model_info_load;
	}

	public void setDisableDefaultModelInfoLoad(boolean disableDefaultModelInfoLoad) {
		this.disable_default_model_info_load = disableDefaultModelInfoLoad;
	}

	public CqlTranslator.Format getTranslatorFormat() {
		return translator_format;
	}

	public void setTranslatorFormat(CqlTranslator.Format translatorFormat) {
		this.translator_format = translatorFormat;
	}
}
