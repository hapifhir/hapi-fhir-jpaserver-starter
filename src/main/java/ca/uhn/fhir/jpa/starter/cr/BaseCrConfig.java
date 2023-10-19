package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.cr.common.CodeCacheResourceChangeListener;
import ca.uhn.fhir.cr.common.ElmCacheResourceChangeListener;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.cache.IResourceChangeListenerRegistry;
import ca.uhn.fhir.jpa.cache.ResourceChangeListenerRegistryInterceptor;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.cql2elm.model.Model;
import org.hl7.cql.model.ModelIdentifier;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class BaseCrConfig {
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

		CodeCacheResourceChangeListener listener = new CodeCacheResourceChangeListener(theDaoRegistry, theEvaluationSettings.getValueSetCache());
		//registry
		theResourceChangeListenerRegistry.registerResourceResourceChangeListener(
			"ValueSet", SearchParameterMap.newSynchronous(), listener,1000);

		return listener;
	}

	// These beans were being duplicated
	// @Bean
	// public IResourceChangeListenerRegistry resourceChangeListenerRegistry(InMemoryResourceMatcher theInMemoryResourceMatcher, FhirContext theFhirContext, ResourceChangeListenerCacheFactory theResourceChangeListenerCacheFactory) {
	// 	return new ResourceChangeListenerRegistryImpl(theFhirContext, theResourceChangeListenerCacheFactory, theInMemoryResourceMatcher);
	// }

	// @Bean
	// IResourceChangeListenerCacheRefresher resourceChangeListenerCacheRefresher() {
	// 	return new ResourceChangeListenerCacheRefresherImpl();
	// }

	@Bean
	public ResourceChangeListenerRegistryInterceptor resourceChangeListenerRegistryInterceptor() {
		return new ResourceChangeListenerRegistryInterceptor();
	}
}
