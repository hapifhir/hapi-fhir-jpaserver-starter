package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.interceptor.model.RequestPartitionId;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.cache.IResourceChangeListenerRegistry;
import ca.uhn.fhir.jpa.cache.ResourceChangeListenerRegistryInterceptor;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
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
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings;
import org.opencds.cqf.fhir.cql.engine.terminology.TerminologySettings;
import org.opencds.cqf.fhir.cr.hapi.common.CodeCacheResourceChangeListener;
import org.opencds.cqf.fhir.cr.hapi.common.ElmCacheResourceChangeListener;
import org.opencds.cqf.fhir.cr.measure.CareGapsProperties;
import org.opencds.cqf.fhir.cr.measure.MeasureEvaluationOptions;
import org.opencds.cqf.fhir.utility.ValidationProfile;
import org.opencds.cqf.fhir.utility.client.TerminologyServerClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Conditional({CrConfigCondition.class})
public class CrCommonConfig {

	@Bean
	RetrieveSettings retrieveSettings(CqlData cqlData) {
		return cqlData.getRetrieveSettings();
	}

	@Bean
	TerminologySettings terminologySettings(CqlTerminologyProperties theCqlTerminologyProperties) {
		return theCqlTerminologyProperties.getTerminologySettings();
	}

	@Bean
	TerminologyServerClientSettings terminologyServerClientSettings(CrProperties theCrProperties) {
		return theCrProperties.getTerminologyServerClientSettings();
	}

	@Bean
	public EvaluationSettings evaluationSettings(
			CqlProperties cqlProperties,
			Map<VersionedIdentifier, CompiledLibrary> theGlobalLibraryCache,
			Map<ModelIdentifier, Model> theGlobalModelCache,
			Map<String, List<Code>> theGlobalValueSetCache) {
		var evaluationSettings = EvaluationSettings.getDefault();
		var cqlOptions = evaluationSettings.getCqlOptions();

		var cqlEngineOptions = cqlOptions.getCqlEngineOptions();
		Set<CqlEngine.Options> options = EnumSet.noneOf(CqlEngine.Options.class);

		if (cqlProperties.getRuntime().isEnableExpressionCaching()) {
			options.add(CqlEngine.Options.EnableExpressionCaching);
		}
		if (cqlProperties.getRuntime().isEnableValidation()) {
			options.add(CqlEngine.Options.EnableValidation);
		}
		cqlEngineOptions.setOptions(options);
		if (cqlProperties.getRuntime().isDebugLoggingEnabled()) {
			cqlEngineOptions.setDebugLoggingEnabled(true);
		}
		cqlOptions.setCqlEngineOptions(cqlEngineOptions);

		var cqlCompilerOptions = new CqlCompilerOptions();

		if (cqlProperties.getCompiler().isEnableDateRangeOptimization()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableDateRangeOptimization);
		}
		if (cqlProperties.getCompiler().isEnableAnnotations()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableAnnotations);
		}
		if (cqlProperties.getCompiler().isEnableLocators()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableLocators);
		}
		if (cqlProperties.getCompiler().isEnableResultsType()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableResultTypes);
		}
		cqlCompilerOptions.setVerifyOnly(cqlProperties.getCompiler().isVerifyOnly());
		if (cqlProperties.getCompiler().isEnableDetailedErrors()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableDetailedErrors);
		}
		cqlCompilerOptions.setErrorLevel(cqlProperties.getCompiler().getErrorSeverityLevel());
		if (cqlProperties.getCompiler().isDisableListTraversal()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListTraversal);
		}
		if (cqlProperties.getCompiler().isDisableListDemotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListDemotion);
		}
		if (cqlProperties.getCompiler().isDisableListPromotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListPromotion);
		}
		if (cqlProperties.getCompiler().isEnableIntervalDemotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableIntervalDemotion);
		}
		if (cqlProperties.getCompiler().isEnableIntervalPromotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableIntervalPromotion);
		}
		if (cqlProperties.getCompiler().isDisableMethodInvocation()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableMethodInvocation);
		}
		if (cqlProperties.getCompiler().isRequireFromKeyword()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.RequireFromKeyword);
		}
		cqlCompilerOptions.setValidateUnits(cqlProperties.getCompiler().isValidateUnits());
		if (cqlProperties.getCompiler().isDisableDefaultModelInfoLoad()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableDefaultModelInfoLoad);
		}
		cqlCompilerOptions.setSignatureLevel(cqlProperties.getCompiler().getSignatureLevel());
		cqlCompilerOptions.setCompatibilityLevel(cqlProperties.getCompiler().getCompatibilityLevel());
		cqlCompilerOptions.setAnalyzeDataRequirements(
				cqlProperties.getCompiler().isAnalyzeDataRequirements());
		cqlCompilerOptions.setCollapseDataRequirements(
				cqlProperties.getCompiler().isCollapseDataRequirements());

		cqlOptions.setCqlCompilerOptions(cqlCompilerOptions);
		evaluationSettings.setLibraryCache(theGlobalLibraryCache);
		evaluationSettings.setModelCache(theGlobalModelCache);
		evaluationSettings.setValueSetCache(theGlobalValueSetCache);
		evaluationSettings.setRetrieveSettings(cqlProperties.getRetrieveSettings());
		evaluationSettings.setTerminologySettings(cqlProperties.getTerminology());
		evaluationSettings.setRegisteredNamespaces(cqlProperties.getNamespaces());
		return evaluationSettings;
	}

	@Bean(name = "measure.CareGapsProperties")
	org.opencds.cqf.fhir.cr.measure.CareGapsProperties careGapsProperties(CrProperties theCrProperties) {
		var careGapsProperties = new CareGapsProperties();
		// This check for the resource type really should be happening down in CR where the setting is actually used but
		// that will have to wait for a future CR release
		careGapsProperties.setCareGapsReporter(
				theCrProperties.getCareGaps().getReporter().replace("Organization/", ""));
		careGapsProperties.setCareGapsCompositionSectionAuthor(
				theCrProperties.getCareGaps().getSection_author().replace("Organization/", ""));
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
				"Library", RequestPartitionId.allPartitions(), SearchParameterMap.newSynchronous(), listener, 1000);
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
				"ValueSet", RequestPartitionId.allPartitions(), SearchParameterMap.newSynchronous(), listener, 1000);

		return listener;
	}

	@Bean
	public ResourceChangeListenerRegistryInterceptor resourceChangeListenerRegistryInterceptor() {
		return new ResourceChangeListenerRegistryInterceptor();
	}
}
