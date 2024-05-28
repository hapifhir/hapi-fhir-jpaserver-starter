package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.cr.common.CodeCacheResourceChangeListener;
import ca.uhn.fhir.cr.common.CqlThreadFactory;
import ca.uhn.fhir.cr.common.ElmCacheResourceChangeListener;
import ca.uhn.fhir.cr.config.r4.ApplyOperationConfig;
import ca.uhn.fhir.cr.config.r4.CrR4Config;
import ca.uhn.fhir.cr.config.r4.ExtractOperationConfig;
import ca.uhn.fhir.cr.config.r4.PackageOperationConfig;
import ca.uhn.fhir.cr.config.r4.PopulateOperationConfig;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.cache.IResourceChangeListenerRegistry;
import ca.uhn.fhir.jpa.cache.ResourceChangeListenerRegistryInterceptor;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.starter.annotations.OnR4Condition;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import org.cqframework.cql.cql2elm.CqlCompilerOptions;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.cql2elm.model.Model;
import org.hl7.cql.model.ModelIdentifier;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cr.measure.CareGapsProperties;
import org.opencds.cqf.fhir.cr.measure.MeasureEvaluationOptions;
import org.opencds.cqf.fhir.utility.ValidationProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Conditional({OnR4Condition.class, CrConfigCondition.class})
@Import({
	CrR4Config.class,
	ApplyOperationConfig.class,
	ExtractOperationConfig.class,
	PackageOperationConfig.class,
	PopulateOperationConfig.class
})
public class StarterCrR4Config {
	private static final Logger ourLogger = LoggerFactory.getLogger(StarterCrR4Config.class);

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
		careGapsProperties.setCareGapsReporter(theCrProperties.getCareGapsReporter());
		careGapsProperties.setCareGapsCompositionSectionAuthor(theCrProperties.getCareGapsSectionAuthor());
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
	public EvaluationSettings evaluationSettings(
			CrProperties theCrProperties,
			Map<VersionedIdentifier, CompiledLibrary> theGlobalLibraryCache,
			Map<ModelIdentifier, Model> theGlobalModelCache,
			Map<String, List<Code>> theGlobalValueSetCache) {
		var evaluationSettings = EvaluationSettings.getDefault();
		var cqlOptions = evaluationSettings.getCqlOptions();

		var cqlEngineOptions = cqlOptions.getCqlEngineOptions();
		Set<CqlEngine.Options> options = EnumSet.noneOf(CqlEngine.Options.class);
		if (theCrProperties.isCqlRuntimeEnableExpressionCaching()) {
			options.add(CqlEngine.Options.EnableExpressionCaching);
		}
		if (theCrProperties.isCqlRuntimeEnableValidation()) {
			options.add(CqlEngine.Options.EnableValidation);
		}
		cqlEngineOptions.setOptions(options);
		cqlOptions.setCqlEngineOptions(cqlEngineOptions);

		var cqlCompilerOptions = new CqlCompilerOptions();

		if (theCrProperties.isEnableDateRangeOptimization()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableDateRangeOptimization);
		}
		if (theCrProperties.isEnableAnnotations()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableAnnotations);
		}
		if (theCrProperties.isEnableLocators()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableLocators);
		}
		if (theCrProperties.isEnableResultsType()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableResultTypes);
		}
		cqlCompilerOptions.setVerifyOnly(theCrProperties.isCqlCompilerVerifyOnly());
		if (theCrProperties.isEnableDetailedErrors()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableDetailedErrors);
		}
		cqlCompilerOptions.setErrorLevel(theCrProperties.getCqlCompilerErrorSeverityLevel());
		if (theCrProperties.isDisableListTraversal()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListTraversal);
		}
		if (theCrProperties.isDisableListDemotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListDemotion);
		}
		if (theCrProperties.isDisableListPromotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListPromotion);
		}
		if (theCrProperties.isEnableIntervalDemotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableIntervalDemotion);
		}
		if (theCrProperties.isEnableIntervalPromotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableIntervalPromotion);
		}
		if (theCrProperties.isDisableMethodInvocation()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableMethodInvocation);
		}
		if (theCrProperties.isRequireFromKeyword()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.RequireFromKeyword);
		}
		cqlCompilerOptions.setValidateUnits(theCrProperties.isCqlCompilerValidateUnits());
		if (theCrProperties.isDisableDefaultModelInfoLoad()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableDefaultModelInfoLoad);
		}
		cqlCompilerOptions.setSignatureLevel(theCrProperties.getCqlCompilerSignatureLevel());
		cqlCompilerOptions.setCompatibilityLevel(theCrProperties.getCqlCompilerCompatibilityLevel());
		cqlCompilerOptions.setAnalyzeDataRequirements(theCrProperties.isCqlCompilerAnalyzeDataRequirements());
		cqlCompilerOptions.setCollapseDataRequirements(theCrProperties.isCqlCompilerCollapseDataRequirements());

		cqlOptions.setCqlCompilerOptions(cqlCompilerOptions);
		evaluationSettings.setLibraryCache(theGlobalLibraryCache);
		evaluationSettings.setModelCache(theGlobalModelCache);
		evaluationSettings.setValueSetCache(theGlobalValueSetCache);
		return evaluationSettings;
	}

	@Bean
	public PostInitProviderRegisterer postInitProviderRegisterer(
			RestfulServer theRestfulServer, ResourceProviderFactory theResourceProviderFactory) {
		return new PostInitProviderRegisterer(theRestfulServer, theResourceProviderFactory);
	}

	@Bean
	public CrProperties crProperties() {
		return new CrProperties();
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
