package ca.uhn.fhir.jpa.starter.common.validation;

import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor;

public interface IRepositoryValidationInterceptorFactory {

	String ENABLE_REPOSITORY_VALIDATING_INTERCEPTOR = "enable_repository_validating_interceptor";

	RepositoryValidatingInterceptor buildUsingStoredStructureDefinitions();

	RepositoryValidatingInterceptor build();
}
