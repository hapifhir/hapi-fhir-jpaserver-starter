package ca.uhn.fhir.jpa.starter.cr;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.cqframework.cql.cql2elm.CqlCompilerOptions;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.cql2elm.model.Model;
import org.hl7.cql.model.ModelIdentifier;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings;
import org.opencds.cqf.fhir.cql.engine.terminology.TerminologySettings;
import org.opencds.cqf.fhir.cr.measure.CareGapsProperties;
import org.opencds.cqf.fhir.cr.measure.MeasureEvaluationOptions;
import org.opencds.cqf.fhir.utility.ValidationProfile;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import ca.uhn.fhir.cr.common.CodeCacheResourceChangeListener;
import ca.uhn.fhir.cr.common.CqlThreadFactory;
import ca.uhn.fhir.cr.common.ElmCacheResourceChangeListener;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.cache.IResourceChangeListenerRegistry;
import ca.uhn.fhir.jpa.cache.ResourceChangeListenerRegistryInterceptor;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;

@Configuration
@Conditional({CrConfigCondition.class})
public class CrCommonConfig {

	@Bean
	@ConfigurationProperties(prefix = "hapi.fhir.cr")
	CrProperties crProperties() {
		return new CrProperties();
	}

	@Bean
	RetrieveSettings retrieveSettings(CrProperties theCrProperties) {
		return theCrProperties.getCql().getData();
	}

	@Bean
	TerminologySettings terminologySettings(CrProperties theCrProperties) {
		return theCrProperties.getCql().getTerminology();
	}

	@Bean
	public EvaluationSettings evaluationSettings(
			CrProperties theCrProperties,
			RetrieveSettings theRetrieveSettings,
			TerminologySettings theTerminologySettings,
			Map<VersionedIdentifier, CompiledLibrary> theGlobalLibraryCache,
			Map<ModelIdentifier, Model> theGlobalModelCache,
			Map<String, List<Code>> theGlobalValueSetCache) {
		var evaluationSettings = EvaluationSettings.getDefault();
		var cqlOptions = evaluationSettings.getCqlOptions();

		var cqlEngineOptions = cqlOptions.getCqlEngineOptions();
		Set<CqlEngine.Options> options = EnumSet.noneOf(CqlEngine.Options.class);
		var cqlRuntimeProperties = theCrProperties.getCql().getRuntime();
		if (cqlRuntimeProperties.isEnableExpressionCaching()) {
			options.add(CqlEngine.Options.EnableExpressionCaching);
		}
		if (cqlRuntimeProperties.isEnableValidation()) {
			options.add(CqlEngine.Options.EnableValidation);
		}
		cqlEngineOptions.setOptions(options);
		if (cqlRuntimeProperties.isDebugLoggingEnabled()) {
			cqlEngineOptions.setDebugLoggingEnabled(true);
		}
		cqlOptions.setCqlEngineOptions(cqlEngineOptions);

		var cqlCompilerOptions = new CqlCompilerOptions();

		var cqlCompilerProperties = theCrProperties.getCql().getCompiler();

		if (cqlCompilerProperties.isEnableDateRangeOptimization()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableDateRangeOptimization);
		}
		if (cqlCompilerProperties.isEnableAnnotations()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableAnnotations);
		}
		if (cqlCompilerProperties.isEnableLocators()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableLocators);
		}
		if (cqlCompilerProperties.isEnableResultsType()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableResultTypes);
		}
		cqlCompilerOptions.setVerifyOnly(cqlCompilerProperties.isVerifyOnly());
		if (cqlCompilerProperties.isEnableDetailedErrors()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableDetailedErrors);
		}
		cqlCompilerOptions.setErrorLevel(cqlCompilerProperties.getErrorSeverityLevel());
		if (cqlCompilerProperties.isDisableListTraversal()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListTraversal);
		}
		if (cqlCompilerProperties.isDisableListDemotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListDemotion);
		}
		if (cqlCompilerProperties.isDisableListPromotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListPromotion);
		}
		if (cqlCompilerProperties.isEnableIntervalDemotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableIntervalDemotion);
		}
		if (cqlCompilerProperties.isEnableIntervalPromotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableIntervalPromotion);
		}
		if (cqlCompilerProperties.isDisableMethodInvocation()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableMethodInvocation);
		}
		if (cqlCompilerProperties.isRequireFromKeyword()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.RequireFromKeyword);
		}
		cqlCompilerOptions.setValidateUnits(cqlCompilerProperties.isValidateUnits());
		if (cqlCompilerProperties.isDisableDefaultModelInfoLoad()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableDefaultModelInfoLoad);
		}
		cqlCompilerOptions.setSignatureLevel(cqlCompilerProperties.getSignatureLevel());
		cqlCompilerOptions.setCompatibilityLevel(cqlCompilerProperties.getCompatibilityLevel());
		cqlCompilerOptions.setAnalyzeDataRequirements(cqlCompilerProperties.isAnalyzeDataRequirements());
		cqlCompilerOptions.setCollapseDataRequirements(cqlCompilerProperties.isCollapseDataRequirements());

		cqlOptions.setCqlCompilerOptions(cqlCompilerOptions);
		evaluationSettings.setLibraryCache(theGlobalLibraryCache);
		evaluationSettings.setModelCache(theGlobalModelCache);
		evaluationSettings.setValueSetCache(theGlobalValueSetCache);
		evaluationSettings.setRetrieveSettings(theRetrieveSettings);
		evaluationSettings.setTerminologySettings(theTerminologySettings);
		return evaluationSettings;
	}

	@Primary
	@Bean
	public ExecutorService cqlExecutor() {
		CqlThreadFactory factory = new CqlThreadFactory();
		ExecutorService executor = Executors.newFixedThreadPool(2, factory);
		executor = new DelegatingSecurityContextExecutorService(executor);

		return executor;
	}

	@Bean
	CareGapsProperties careGapsProperties(CrProperties theCrProperties) {
		var careGapsProperties = new CareGapsProperties();
		careGapsProperties.setCareGapsReporter(theCrProperties.getCareGaps().getReporter());
		careGapsProperties.setCareGapsCompositionSectionAuthor(theCrProperties.getCareGaps().getSection_author());
		return careGapsProperties;
	}

	@Bean
	MeasureEvaluationOptions measureEvaluationOptions(
			EvaluationSettings theEvaluationSettings, Map<String, ValidationProfile> theValidationProfiles) {
		MeasureEvaluationOptions measureEvalOptions = new MeasureEvaluationOptions();
		measureEvalOptions.setEvaluationSettings(theEvaluationSettings);
		if (measureEvalOptions.isValidationEnabled()) {
			measureEvalOptions.setValidationProfiles(theValidationProfiles);
		}
		return measureEvalOptions;
	}

	@Bean
	public PostInitProviderRegisterer postInitProviderRegisterer(
			RestfulServer theRestfulServer, ResourceProviderFactory theResourceProviderFactory) {
		return new PostInitProviderRegisterer(theRestfulServer, theResourceProviderFactory);
	}

	@Bean
	public Map<VersionedIdentifier, CompiledLibrary> globalLibraryCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	public Map<ModelIdentifier, Model> globalModelCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	public Map<String, List<Code>> globalValueSetCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	public ElmCacheResourceChangeListener elmCacheResourceChangeListener(
			IResourceChangeListenerRegistry theResourceChangeListenerRegistry,
			DaoRegistry theDaoRegistry,
			EvaluationSettings theEvaluationSettings) {
		ElmCacheResourceChangeListener listener =
				new ElmCacheResourceChangeListener(theDaoRegistry, theEvaluationSettings.getLibraryCache());
		theResourceChangeListenerRegistry.registerResourceResourceChangeListener(
				"Library", SearchParameterMap.newSynchronous(), listener, 1000);
		return listener;
	}

	@Bean
	public CodeCacheResourceChangeListener codeCacheResourceChangeListener(
			IResourceChangeListenerRegistry theResourceChangeListenerRegistry,
			EvaluationSettings theEvaluationSettings,
			DaoRegistry theDaoRegistry) {

		CodeCacheResourceChangeListener listener =
				new CodeCacheResourceChangeListener(theDaoRegistry, theEvaluationSettings.getValueSetCache());
		// registry
		theResourceChangeListenerRegistry.registerResourceResourceChangeListener(
				"ValueSet", SearchParameterMap.newSynchronous(), listener, 1000);

		return listener;
	}

	@Bean
	public ResourceChangeListenerRegistryInterceptor resourceChangeListenerRegistryInterceptor() {
		return new ResourceChangeListenerRegistryInterceptor();
	}   
}
