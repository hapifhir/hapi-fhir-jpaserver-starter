package ca.uhn.fhir.jpa.starter.cr;

public class CqlRuntimeProperties {

    private Boolean debug_logging_enabled = false;
	private Boolean enable_validation = false;
	private Boolean enable_expression_caching = true;

	public boolean isDebugLoggingEnabled() {
		return debug_logging_enabled;
	}

	public void setDebugLoggingEnabled(boolean debug_logging_enabled) {
		this.debug_logging_enabled = debug_logging_enabled;
	}


	public boolean isEnableExpressionCaching() {
		return enable_expression_caching;
	}

	public void setEnableExpressionCaching(boolean enable_expression_caching) {
		this.enable_expression_caching = enable_expression_caching;
	}

	public boolean isEnableValidation() {
		return enable_validation;
	}

	public void EnableValidation(boolean enable_validation) {
		this.enable_validation = enable_validation;
	}

    
}
