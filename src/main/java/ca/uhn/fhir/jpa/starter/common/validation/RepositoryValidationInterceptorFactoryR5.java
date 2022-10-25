package ca.uhn.fhir.jpa.starter.common.validation;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.jpa.interceptor.validation.IRepositoryValidatingRule;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingInterceptor;
import ca.uhn.fhir.jpa.interceptor.validation.RepositoryValidatingRuleBuilder;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.jpa.starter.annotations.OnR5Condition;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ca.uhn.fhir.jpa.starter.common.validation.IRepositoryValidationInterceptorFactory.ENABLE_REPOSITORY_VALIDATING_INTERCEPTOR;

/**
 * This class can be customized to enable the {@link RepositoryValidatingInterceptor}
 * on this server.
 * <p>
 * The <code>enable_repository_validating_interceptor</code> property must be enabled in <code>application.yaml</code>
 * in order to use this class.
 */
@ConditionalOnProperty(prefix = "hapi.fhir", name = ENABLE_REPOSITORY_VALIDATING_INTERCEPTOR, havingValue = "true")
@Configuration
@Conditional(OnR5Condition.class)
public class RepositoryValidationInterceptorFactoryR5 implements IRepositoryValidationInterceptorFactory {

	private final FhirContext fhirContext;
	private final RepositoryValidatingRuleBuilder repositoryValidatingRuleBuilder;
	private final IFhirResourceDao structureDefinitionResourceProvider;

	public RepositoryValidationInterceptorFactoryR5(RepositoryValidatingRuleBuilder repositoryValidatingRuleBuilder, DaoRegistry daoRegistry) {
		this.repositoryValidatingRuleBuilder = repositoryValidatingRuleBuilder;
		this.fhirContext = daoRegistry.getSystemDao().getContext();
		structureDefinitionResourceProvider = daoRegistry.getResourceDao("StructureDefinition");

	}

	public RepositoryValidatingInterceptor buildUsingStoredStructureDefinitions() {

		IBundleProvider results = structureDefinitionResourceProvider.search(new SearchParameterMap().add(StructureDefinition.SP_KIND, new TokenParam("resource")));
		Map<String, List<StructureDefinition>> structureDefintions = results.getResources(0, results.size())
			.stream()
			.map(StructureDefinition.class::cast)
			.collect(Collectors.groupingBy(StructureDefinition::getType));

		structureDefintions.forEach((key, value) -> {
			String[] urls = value.stream().map(StructureDefinition::getUrl).toArray(String[]::new);
			repositoryValidatingRuleBuilder.forResourcesOfType(key).requireAtLeastOneProfileOf(urls).and().requireValidationToDeclaredProfiles();
		});

		List<IRepositoryValidatingRule> rules = repositoryValidatingRuleBuilder.build();
		return new RepositoryValidatingInterceptor(fhirContext, rules);
	}

	public RepositoryValidatingInterceptor build() {

		// Customize the ruleBuilder here to have the rules you want! We will give a simple example
		// of enabling validation for all Patient resources
		repositoryValidatingRuleBuilder.forResourcesOfType("Patient").requireAtLeastProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient").and().requireValidationToDeclaredProfiles();

		// Do not customize below this line
		List<IRepositoryValidatingRule> rules = repositoryValidatingRuleBuilder.build();
		return new RepositoryValidatingInterceptor(fhirContext, rules);
	}

}
