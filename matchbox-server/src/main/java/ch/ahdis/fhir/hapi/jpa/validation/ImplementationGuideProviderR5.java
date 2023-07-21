package ch.ahdis.fhir.hapi.jpa.validation;

import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionDao;
import ca.uhn.fhir.jpa.model.entity.NpmPackageVersionEntity;
import ca.uhn.fhir.jpa.packages.PackageDeleteOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallOutcomeJson;
import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.rp.r5.ImplementationGuideResourceProvider;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.*;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import ch.ahdis.matchbox.MatchboxEngineSupport;
import ch.ahdis.matchbox.engine.cli.VersionUtil;
import ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.Enumerations.FHIRVersion;
import org.quartz.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * $load and $load-all Operation for ImplementationGuide Resource (R5)
 *
 */
@DisallowConcurrentExecution
public class ImplementationGuideProviderR5 extends ImplementationGuideResourceProvider
		implements Job, ApplicationContextAware {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ImplementationGuideProviderR5.class);

	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;

	@Override
	public MethodOutcome delete(HttpServletRequest theRequest, IIdType theResource, String theConditional,
			RequestDetails theRequestDetails) {
		ImplementationGuide guide = new ImplementationGuide();
		int pos = theResource.getIdPart().lastIndexOf("-");
		String version = theResource.getIdPart().substring(pos + 1);
		int space = version.indexOf(' ');
		if (space > 0) {
			version = version.substring(0, space);
		}
		guide.setVersion(version);
		String name = theResource.getIdPart().substring(0, pos);
		guide.setName(name);
		MethodOutcome outcome = new MethodOutcome();
		OperationOutcome oo = uninstall(guide);
		outcome.setOperationOutcome(oo);
		return outcome;
	}

	@Override
	public MethodOutcome update(HttpServletRequest theRequest, ImplementationGuide theResource, IIdType theId,
			String theConditional, RequestDetails theRequestDetails) {
		OperationOutcome oo = load(theResource);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setOperationOutcome(oo);

		// initialize matchbox engine
		log.info("Initializing matchbox engine(s): " + VersionUtil.getMemory());
		matchboxEngineSupport.getMatchboxEngine(FHIRVersion._4_0_1.getDisplay(), null, false, true);
		log.info("Initializing matchbox engine finished: " + VersionUtil.getMemory());

		return outcome;
	}

	@Override
	public MethodOutcome create(HttpServletRequest theRequest, ImplementationGuide theResource, String theConditional,
			RequestDetails theRequestDetails) {
		OperationOutcome oo = load(theResource);
		MethodOutcome outcome = new MethodOutcome();
		outcome.setOperationOutcome(oo);
		return outcome;
	}

	@Autowired
	MatchboxPackageInstallerImpl packageInstallerSvc;

	@Autowired
	AppProperties appProperties;

	@Autowired
	private INpmPackageVersionDao myPackageVersionDao;

	@Autowired
	private PlatformTransactionManager myTxManager;

	public OperationOutcome getOperationOutcome(PackageInstallOutcomeJson pkgOutcome) {
		if (pkgOutcome == null) {
			return null;
		}
		OperationOutcome outcome = new OperationOutcome();
		for (String message : pkgOutcome.getMessage()) {
			outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING)
					.setDiagnostics(message);
		}
		for (String resource : pkgOutcome.getResourcesInstalled().keySet()) {
			outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING)
					.setDiagnostics(resource + ": " + pkgOutcome.getResourcesInstalled().get(resource));
		}
		return outcome;
	}

	public OperationOutcome getOperationOutcome(PackageDeleteOutcomeJson pkgOutcome) {
		if (pkgOutcome == null) {
			return null;
		}
		OperationOutcome outcome = new OperationOutcome();
		for (String message : pkgOutcome.getMessage()) {
			outcome.addIssue().setSeverity(IssueSeverity.INFORMATION).setCode(IssueType.PROCESSING)
					.setDiagnostics(message);
		}
		return outcome;
	}

	public OperationOutcome uninstall(ImplementationGuide theResource) {
		return getOperationOutcome(packageInstallerSvc.uninstall(this.getPackageInstallationSpec()
				.setPackageUrl(theResource.getUrl())
				.setName(theResource.getName())
				.setVersion(theResource.getVersion())));
	}

	public PackageInstallationSpec getPackageInstallationSpec() {
		return new PackageInstallationSpec()
				.addInstallResourceTypes(MatchboxPackageInstallerImpl.DEFAULT_INSTALL_TYPES.toArray(new String[0]))
				.setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_ONLY)
				.addDependencyExclude("hl7.fhir.r5.core")
				.addDependencyExclude("hl7.terminology")
				.addDependencyExclude("hl7.terminology.r5")
				.addDependencyExclude("hl7.fhir.r5.examples");
	}

	public PackageInstallOutcomeJson load(ImplementationGuide theResource, PackageInstallOutcomeJson install) {
		PackageInstallOutcomeJson installOutcome = packageInstallerSvc
				.install(this.getPackageInstallationSpec().setName(theResource.getName())
						.setPackageUrl(theResource.getUrl()).setVersion(theResource.getVersion()));
		if (install != null) {
			install.getMessage().addAll(installOutcome.getMessage());
			return install;
		}
		return installOutcome;
	}

	public OperationOutcome load(ImplementationGuide theResource) {
		PackageInstallOutcomeJson installOutcome = packageInstallerSvc.install(this.getPackageInstallationSpec()
				.setPackageUrl(theResource.getUrl())
				.addInstallResourceTypes(MatchboxPackageInstallerImpl.DEFAULT_INSTALL_TYPES.toArray(new String[0]))
				.setName(theResource.getName())
				.setVersion(theResource.getVersion())
				.setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_ONLY));
		return getOperationOutcome(installOutcome);
	}

	public PackageInstallOutcomeJson loadAll(boolean replace) {
		matchboxEngineSupport.setInitialized(false);
		log.info("Initializing packages " + VersionUtil.getMemory());
		PackageInstallOutcomeJson installOutcome = null;
		if (appProperties.getImplementationGuides() != null) {
			Map<String, AppProperties.ImplementationGuide> guides = appProperties.getImplementationGuides();
			for (AppProperties.ImplementationGuide guide : guides.values()) {
				boolean exists = new TransactionTemplate(myTxManager).execute(tx -> {
					Optional<NpmPackageVersionEntity> existing = myPackageVersionDao
							.findByPackageIdAndVersion(guide.getName(), guide.getVersion());
					return existing.isPresent();
				});
				if (!exists || replace) {
					ImplementationGuide ig = new ImplementationGuide();
					ig.setName(guide.getName());
					ig.setPackageId(guide.getName());
					ig.setUrl(guide.getUrl());
					ig.setVersion(guide.getVersion());
					installOutcome = load(ig, installOutcome);
				}
			}
		}
		matchboxEngineSupport.setInitialized(true);
		log.info("Initializing packages finished " + VersionUtil.getMemory());
		log.info("Creating cached engines during startup  " + VersionUtil.getMemory());
		matchboxEngineSupport.getMatchboxEngine(null,null, false, false);
		log.info("Finished engines during startup  " + VersionUtil.getMemory());
		return installOutcome;
	}

	@Operation(name = "$load-all", type = ImplementationGuide.class, idempotent = false)
	public OperationOutcome loadAll() {
		return this.getOperationOutcome(loadAll(true));
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			context.getScheduler().unscheduleJob(context.getTrigger().getKey());
			this.loadAll(false);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	@Override
	@Search(
		allowUnknownParams = true
	)
	public IBundleProvider search(HttpServletRequest theServletRequest,
											HttpServletResponse theServletResponse,
											RequestDetails theRequestDetails,
											@Description(shortDefinition = "Search the contents of the resource's data using a filter") @OptionalParam(name = "_filter") StringAndListParam theFtFilter,
											@Description(shortDefinition = "Search the contents of the resource's data using a fulltext search") @OptionalParam(name = "_content") StringAndListParam theFtContent,
											@Description(shortDefinition = "Search the contents of the resource's narrative using a fulltext search") @OptionalParam(name = "_text") StringAndListParam theFtText,
											@Description(shortDefinition = "Search for resources which have the given tag") @OptionalParam(name = "_tag") TokenAndListParam theSearchForTag,
											@Description(shortDefinition = "Search for resources which have the given security labels") @OptionalParam(name = "_security") TokenAndListParam theSearchForSecurity,
											@Description(shortDefinition = "Search for resources which have the given profile") @OptionalParam(name = "_profile") UriAndListParam theSearchForProfile,
											@Description(shortDefinition = "Search for resources which have the given source value (Resource.meta.source)") @OptionalParam(name = "_source") UriAndListParam theSearchForSource,
											@Description(shortDefinition = "Return resources linked to by the given target") @OptionalParam(name = "_has") HasAndListParam theHas,
											@Description(shortDefinition = "The ID of the resource") @OptionalParam(name = "_id") TokenAndListParam the_id,
											@Description(shortDefinition = "Search on the narrative of the resource") @OptionalParam(name = "_text") SpecialAndListParam the_text,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): A use context assigned to the activity definition* [ActorDefinition](actordefinition.html): A use context assigned to the Actor Definition* [CapabilityStatement](capabilitystatement.html): A use context assigned to the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): A use context assigned to the charge item definition* [Citation](citation.html): A use context assigned to the citation* [CodeSystem](codesystem.html): A use context assigned to the code system* [CompartmentDefinition](compartmentdefinition.html): A use context assigned to the compartment definition* [ConceptMap](conceptmap.html): A use context assigned to the concept map* [ConditionDefinition](conditiondefinition.html): A use context assigned to the condition definition* [EventDefinition](eventdefinition.html): A use context assigned to the event definition* [Evidence](evidence.html): A use context assigned to the evidence* [EvidenceReport](evidencereport.html): A use context assigned to the evidence report* [EvidenceVariable](evidencevariable.html): A use context assigned to the evidence variable* [ExampleScenario](examplescenario.html): A use context assigned to the example scenario* [GraphDefinition](graphdefinition.html): A use context assigned to the graph definition* [ImplementationGuide](implementationguide.html): A use context assigned to the implementation guide* [Library](library.html): A use context assigned to the library* [Measure](measure.html): A use context assigned to the measure* [MessageDefinition](messagedefinition.html): A use context assigned to the message definition* [NamingSystem](namingsystem.html): A use context assigned to the naming system* [OperationDefinition](operationdefinition.html): A use context assigned to the operation definition* [PlanDefinition](plandefinition.html): A use context assigned to the plan definition* [Questionnaire](questionnaire.html): A use context assigned to the questionnaire* [Requirements](requirements.html): A use context assigned to the requirements* [SearchParameter](searchparameter.html): A use context assigned to the search parameter* [StructureDefinition](structuredefinition.html): A use context assigned to the structure definition* [StructureMap](structuremap.html): A use context assigned to the structure map* [TerminologyCapabilities](terminologycapabilities.html): A use context assigned to the terminology capabilities* [TestScript](testscript.html): A use context assigned to the test script* [ValueSet](valueset.html): A use context assigned to the value set") @OptionalParam(name = "context") TokenAndListParam theContext,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): A quantity- or range-valued use context assigned to the activity definition* [ActorDefinition](actordefinition.html): A quantity- or range-valued use context assigned to the Actor Definition* [CapabilityStatement](capabilitystatement.html): A quantity- or range-valued use context assigned to the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): A quantity- or range-valued use context assigned to the charge item definition* [Citation](citation.html): A quantity- or range-valued use context assigned to the citation* [CodeSystem](codesystem.html): A quantity- or range-valued use context assigned to the code system* [CompartmentDefinition](compartmentdefinition.html): A quantity- or range-valued use context assigned to the compartment definition* [ConceptMap](conceptmap.html): A quantity- or range-valued use context assigned to the concept map* [ConditionDefinition](conditiondefinition.html): A quantity- or range-valued use context assigned to the condition definition* [EventDefinition](eventdefinition.html): A quantity- or range-valued use context assigned to the event definition* [Evidence](evidence.html): A quantity- or range-valued use context assigned to the evidence* [EvidenceReport](evidencereport.html): A quantity- or range-valued use context assigned to the evidence report* [EvidenceVariable](evidencevariable.html): A quantity- or range-valued use context assigned to the evidence variable* [ExampleScenario](examplescenario.html): A quantity- or range-valued use context assigned to the example scenario* [GraphDefinition](graphdefinition.html): A quantity- or range-valued use context assigned to the graph definition* [ImplementationGuide](implementationguide.html): A quantity- or range-valued use context assigned to the implementation guide* [Library](library.html): A quantity- or range-valued use context assigned to the library* [Measure](measure.html): A quantity- or range-valued use context assigned to the measure* [MessageDefinition](messagedefinition.html): A quantity- or range-valued use context assigned to the message definition* [NamingSystem](namingsystem.html): A quantity- or range-valued use context assigned to the naming system* [OperationDefinition](operationdefinition.html): A quantity- or range-valued use context assigned to the operation definition* [PlanDefinition](plandefinition.html): A quantity- or range-valued use context assigned to the plan definition* [Questionnaire](questionnaire.html): A quantity- or range-valued use context assigned to the questionnaire* [Requirements](requirements.html): A quantity- or range-valued use context assigned to the requirements* [SearchParameter](searchparameter.html): A quantity- or range-valued use context assigned to the search parameter* [StructureDefinition](structuredefinition.html): A quantity- or range-valued use context assigned to the structure definition* [StructureMap](structuremap.html): A quantity- or range-valued use context assigned to the structure map* [TerminologyCapabilities](terminologycapabilities.html): A quantity- or range-valued use context assigned to the terminology capabilities* [TestScript](testscript.html): A quantity- or range-valued use context assigned to the test script* [ValueSet](valueset.html): A quantity- or range-valued use context assigned to the value set") @OptionalParam(name = "context-quantity") QuantityAndListParam theContext_quantity,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): A type of use context assigned to the activity definition* [ActorDefinition](actordefinition.html): A type of use context assigned to the Actor Definition* [CapabilityStatement](capabilitystatement.html): A type of use context assigned to the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): A type of use context assigned to the charge item definition* [Citation](citation.html): A type of use context assigned to the citation* [CodeSystem](codesystem.html): A type of use context assigned to the code system* [CompartmentDefinition](compartmentdefinition.html): A type of use context assigned to the compartment definition* [ConceptMap](conceptmap.html): A type of use context assigned to the concept map* [ConditionDefinition](conditiondefinition.html): A type of use context assigned to the condition definition* [EventDefinition](eventdefinition.html): A type of use context assigned to the event definition* [Evidence](evidence.html): A type of use context assigned to the evidence* [EvidenceReport](evidencereport.html): A type of use context assigned to the evidence report* [EvidenceVariable](evidencevariable.html): A type of use context assigned to the evidence variable* [ExampleScenario](examplescenario.html): A type of use context assigned to the example scenario* [GraphDefinition](graphdefinition.html): A type of use context assigned to the graph definition* [ImplementationGuide](implementationguide.html): A type of use context assigned to the implementation guide* [Library](library.html): A type of use context assigned to the library* [Measure](measure.html): A type of use context assigned to the measure* [MessageDefinition](messagedefinition.html): A type of use context assigned to the message definition* [NamingSystem](namingsystem.html): A type of use context assigned to the naming system* [OperationDefinition](operationdefinition.html): A type of use context assigned to the operation definition* [PlanDefinition](plandefinition.html): A type of use context assigned to the plan definition* [Questionnaire](questionnaire.html): A type of use context assigned to the questionnaire* [Requirements](requirements.html): A type of use context assigned to the requirements* [SearchParameter](searchparameter.html): A type of use context assigned to the search parameter* [StructureDefinition](structuredefinition.html): A type of use context assigned to the structure definition* [StructureMap](structuremap.html): A type of use context assigned to the structure map* [TerminologyCapabilities](terminologycapabilities.html): A type of use context assigned to the terminology capabilities* [TestScript](testscript.html): A type of use context assigned to the test script* [ValueSet](valueset.html): A type of use context assigned to the value set") @OptionalParam(name = "context-type") TokenAndListParam theContext_type,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): A use context type and quantity- or range-based value assigned to the activity definition* [ActorDefinition](actordefinition.html): A use context type and quantity- or range-based value assigned to the Actor Definition* [CapabilityStatement](capabilitystatement.html): A use context type and quantity- or range-based value assigned to the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): A use context type and quantity- or range-based value assigned to the charge item definition* [Citation](citation.html): A use context type and quantity- or range-based value assigned to the citation* [CodeSystem](codesystem.html): A use context type and quantity- or range-based value assigned to the code system* [CompartmentDefinition](compartmentdefinition.html): A use context type and quantity- or range-based value assigned to the compartment definition* [ConceptMap](conceptmap.html): A use context type and quantity- or range-based value assigned to the concept map* [ConditionDefinition](conditiondefinition.html): A use context type and quantity- or range-based value assigned to the condition definition* [EventDefinition](eventdefinition.html): A use context type and quantity- or range-based value assigned to the event definition* [Evidence](evidence.html): A use context type and quantity- or range-based value assigned to the evidence* [EvidenceReport](evidencereport.html): A use context type and quantity- or range-based value assigned to the evidence report* [EvidenceVariable](evidencevariable.html): A use context type and quantity- or range-based value assigned to the evidence variable* [ExampleScenario](examplescenario.html): A use context type and quantity- or range-based value assigned to the example scenario* [GraphDefinition](graphdefinition.html): A use context type and quantity- or range-based value assigned to the graph definition* [ImplementationGuide](implementationguide.html): A use context type and quantity- or range-based value assigned to the implementation guide* [Library](library.html): A use context type and quantity- or range-based value assigned to the library* [Measure](measure.html): A use context type and quantity- or range-based value assigned to the measure* [MessageDefinition](messagedefinition.html): A use context type and quantity- or range-based value assigned to the message definition* [NamingSystem](namingsystem.html): A use context type and quantity- or range-based value assigned to the naming system* [OperationDefinition](operationdefinition.html): A use context type and quantity- or range-based value assigned to the operation definition* [PlanDefinition](plandefinition.html): A use context type and quantity- or range-based value assigned to the plan definition* [Questionnaire](questionnaire.html): A use context type and quantity- or range-based value assigned to the questionnaire* [Requirements](requirements.html): A use context type and quantity- or range-based value assigned to the requirements* [SearchParameter](searchparameter.html): A use context type and quantity- or range-based value assigned to the search parameter* [StructureDefinition](structuredefinition.html): A use context type and quantity- or range-based value assigned to the structure definition* [StructureMap](structuremap.html): A use context type and quantity- or range-based value assigned to the structure map* [TerminologyCapabilities](terminologycapabilities.html): A use context type and quantity- or range-based value assigned to the terminology capabilities* [TestScript](testscript.html): A use context type and quantity- or range-based value assigned to the test script* [ValueSet](valueset.html): A use context type and quantity- or range-based value assigned to the value set") @OptionalParam(name = "context-type-quantity",compositeTypes = {TokenParam.class, QuantityParam.class}) CompositeAndListParam<TokenParam, QuantityParam> theContext_type_quantity,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): A use context type and value assigned to the activity definition* [ActorDefinition](actordefinition.html): A use context type and value assigned to the Actor Definition* [CapabilityStatement](capabilitystatement.html): A use context type and value assigned to the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): A use context type and value assigned to the charge item definition* [Citation](citation.html): A use context type and value assigned to the citation* [CodeSystem](codesystem.html): A use context type and value assigned to the code system* [CompartmentDefinition](compartmentdefinition.html): A use context type and value assigned to the compartment definition* [ConceptMap](conceptmap.html): A use context type and value assigned to the concept map* [ConditionDefinition](conditiondefinition.html): A use context type and value assigned to the condition definition* [EventDefinition](eventdefinition.html): A use context type and value assigned to the event definition* [Evidence](evidence.html): A use context type and value assigned to the evidence* [EvidenceReport](evidencereport.html): A use context type and value assigned to the evidence report* [EvidenceVariable](evidencevariable.html): A use context type and value assigned to the evidence variable* [ExampleScenario](examplescenario.html): A use context type and value assigned to the example scenario* [GraphDefinition](graphdefinition.html): A use context type and value assigned to the graph definition* [ImplementationGuide](implementationguide.html): A use context type and value assigned to the implementation guide* [Library](library.html): A use context type and value assigned to the library* [Measure](measure.html): A use context type and value assigned to the measure* [MessageDefinition](messagedefinition.html): A use context type and value assigned to the message definition* [NamingSystem](namingsystem.html): A use context type and value assigned to the naming system* [OperationDefinition](operationdefinition.html): A use context type and value assigned to the operation definition* [PlanDefinition](plandefinition.html): A use context type and value assigned to the plan definition* [Questionnaire](questionnaire.html): A use context type and value assigned to the questionnaire* [Requirements](requirements.html): A use context type and value assigned to the requirements* [SearchParameter](searchparameter.html): A use context type and value assigned to the search parameter* [StructureDefinition](structuredefinition.html): A use context type and value assigned to the structure definition* [StructureMap](structuremap.html): A use context type and value assigned to the structure map* [TerminologyCapabilities](terminologycapabilities.html): A use context type and value assigned to the terminology capabilities* [TestScript](testscript.html): A use context type and value assigned to the test script* [ValueSet](valueset.html): A use context type and value assigned to the value set") @OptionalParam(name = "context-type-value",compositeTypes = {TokenParam.class, TokenParam.class}) CompositeAndListParam<TokenParam, TokenParam> theContext_type_value,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): The activity definition publication date* [ActorDefinition](actordefinition.html): The Actor Definition publication date* [CapabilityStatement](capabilitystatement.html): The capability statement publication date* [ChargeItemDefinition](chargeitemdefinition.html): The charge item definition publication date* [Citation](citation.html): The citation publication date* [CodeSystem](codesystem.html): The code system publication date* [CompartmentDefinition](compartmentdefinition.html): The compartment definition publication date* [ConceptMap](conceptmap.html): The concept map publication date* [ConditionDefinition](conditiondefinition.html): The condition definition publication date* [EventDefinition](eventdefinition.html): The event definition publication date* [Evidence](evidence.html): The evidence publication date* [EvidenceVariable](evidencevariable.html): The evidence variable publication date* [ExampleScenario](examplescenario.html): The example scenario publication date* [GraphDefinition](graphdefinition.html): The graph definition publication date* [ImplementationGuide](implementationguide.html): The implementation guide publication date* [Library](library.html): The library publication date* [Measure](measure.html): The measure publication date* [MessageDefinition](messagedefinition.html): The message definition publication date* [NamingSystem](namingsystem.html): The naming system publication date* [OperationDefinition](operationdefinition.html): The operation definition publication date* [PlanDefinition](plandefinition.html): The plan definition publication date* [Questionnaire](questionnaire.html): The questionnaire publication date* [Requirements](requirements.html): The requirements publication date* [SearchParameter](searchparameter.html): The search parameter publication date* [StructureDefinition](structuredefinition.html): The structure definition publication date* [StructureMap](structuremap.html): The structure map publication date* [SubscriptionTopic](subscriptiontopic.html): Date status first applied* [TerminologyCapabilities](terminologycapabilities.html): The terminology capabilities publication date* [TestScript](testscript.html): The test script publication date* [ValueSet](valueset.html): The value set publication date") @OptionalParam(name = "date") DateRangeParam theDate,
											@Description(shortDefinition = "Identity of the IG that this depends on") @OptionalParam(name = "depends-on",targetTypes = {}) ReferenceAndListParam theDepends_on,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): The description of the activity definition* [ActorDefinition](actordefinition.html): The description of the Actor Definition* [CapabilityStatement](capabilitystatement.html): The description of the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): The description of the charge item definition* [Citation](citation.html): The description of the citation* [CodeSystem](codesystem.html): The description of the code system* [CompartmentDefinition](compartmentdefinition.html): The description of the compartment definition* [ConceptMap](conceptmap.html): The description of the concept map* [ConditionDefinition](conditiondefinition.html): The description of the condition definition* [EventDefinition](eventdefinition.html): The description of the event definition* [Evidence](evidence.html): The description of the evidence* [EvidenceVariable](evidencevariable.html): The description of the evidence variable* [GraphDefinition](graphdefinition.html): The description of the graph definition* [ImplementationGuide](implementationguide.html): The description of the implementation guide* [Library](library.html): The description of the library* [Measure](measure.html): The description of the measure* [MessageDefinition](messagedefinition.html): The description of the message definition* [NamingSystem](namingsystem.html): The description of the naming system* [OperationDefinition](operationdefinition.html): The description of the operation definition* [PlanDefinition](plandefinition.html): The description of the plan definition* [Questionnaire](questionnaire.html): The description of the questionnaire* [Requirements](requirements.html): The description of the requirements* [SearchParameter](searchparameter.html): The description of the search parameter* [StructureDefinition](structuredefinition.html): The description of the structure definition* [StructureMap](structuremap.html): The description of the structure map* [TerminologyCapabilities](terminologycapabilities.html): The description of the terminology capabilities* [TestScript](testscript.html): The description of the test script* [ValueSet](valueset.html): The description of the value set") @OptionalParam(name = "description") StringAndListParam theDescription,
											@Description(shortDefinition = "For testing purposes, not real usage") @OptionalParam(name = "experimental") TokenAndListParam theExperimental,
											@Description(shortDefinition = "Profile that all resources must conform to") @OptionalParam(name = "global",targetTypes = {}) ReferenceAndListParam theGlobal,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): External identifier for the activity definition* [ActorDefinition](actordefinition.html): External identifier for the Actor Definition* [CapabilityStatement](capabilitystatement.html): External identifier for the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): External identifier for the charge item definition* [Citation](citation.html): External identifier for the citation* [CodeSystem](codesystem.html): External identifier for the code system* [ConceptMap](conceptmap.html): External identifier for the concept map* [ConditionDefinition](conditiondefinition.html): External identifier for the condition definition* [EventDefinition](eventdefinition.html): External identifier for the event definition* [Evidence](evidence.html): External identifier for the evidence* [EvidenceReport](evidencereport.html): External identifier for the evidence report* [EvidenceVariable](evidencevariable.html): External identifier for the evidence variable* [ExampleScenario](examplescenario.html): External identifier for the example scenario* [GraphDefinition](graphdefinition.html): External identifier for the graph definition* [ImplementationGuide](implementationguide.html): External identifier for the implementation guide* [Library](library.html): External identifier for the library* [Measure](measure.html): External identifier for the measure* [MedicationKnowledge](medicationknowledge.html): Business identifier for this medication* [MessageDefinition](messagedefinition.html): External identifier for the message definition* [NamingSystem](namingsystem.html): External identifier for the naming system* [ObservationDefinition](observationdefinition.html): The unique identifier associated with the specimen definition* [OperationDefinition](operationdefinition.html): External identifier for the search parameter* [PlanDefinition](plandefinition.html): External identifier for the plan definition* [Questionnaire](questionnaire.html): External identifier for the questionnaire* [Requirements](requirements.html): External identifier for the requirements* [SearchParameter](searchparameter.html): External identifier for the search parameter* [SpecimenDefinition](specimendefinition.html): The unique identifier associated with the SpecimenDefinition* [StructureDefinition](structuredefinition.html): External identifier for the structure definition* [StructureMap](structuremap.html): External identifier for the structure map* [SubscriptionTopic](subscriptiontopic.html): Business Identifier for SubscriptionTopic* [TerminologyCapabilities](terminologycapabilities.html): External identifier for the terminology capabilities* [TestPlan](testplan.html): An identifier for the test plan* [TestScript](testscript.html): External identifier for the test script* [ValueSet](valueset.html): External identifier for the value set") @OptionalParam(name = "identifier") TokenAndListParam theIdentifier,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): Intended jurisdiction for the activity definition* [ActorDefinition](actordefinition.html): Intended jurisdiction for the Actor Definition* [CapabilityStatement](capabilitystatement.html): Intended jurisdiction for the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): Intended jurisdiction for the charge item definition* [Citation](citation.html): Intended jurisdiction for the citation* [CodeSystem](codesystem.html): Intended jurisdiction for the code system* [ConceptMap](conceptmap.html): Intended jurisdiction for the concept map* [ConditionDefinition](conditiondefinition.html): Intended jurisdiction for the condition definition* [EventDefinition](eventdefinition.html): Intended jurisdiction for the event definition* [ExampleScenario](examplescenario.html): Intended jurisdiction for the example scenario* [GraphDefinition](graphdefinition.html): Intended jurisdiction for the graph definition* [ImplementationGuide](implementationguide.html): Intended jurisdiction for the implementation guide* [Library](library.html): Intended jurisdiction for the library* [Measure](measure.html): Intended jurisdiction for the measure* [MessageDefinition](messagedefinition.html): Intended jurisdiction for the message definition* [NamingSystem](namingsystem.html): Intended jurisdiction for the naming system* [OperationDefinition](operationdefinition.html): Intended jurisdiction for the operation definition* [PlanDefinition](plandefinition.html): Intended jurisdiction for the plan definition* [Questionnaire](questionnaire.html): Intended jurisdiction for the questionnaire* [Requirements](requirements.html): Intended jurisdiction for the requirements* [SearchParameter](searchparameter.html): Intended jurisdiction for the search parameter* [StructureDefinition](structuredefinition.html): Intended jurisdiction for the structure definition* [StructureMap](structuremap.html): Intended jurisdiction for the structure map* [TerminologyCapabilities](terminologycapabilities.html): Intended jurisdiction for the terminology capabilities* [TestScript](testscript.html): Intended jurisdiction for the test script* [ValueSet](valueset.html): Intended jurisdiction for the value set") @OptionalParam(name = "jurisdiction") TokenAndListParam theJurisdiction,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): Computationally friendly name of the activity definition* [CapabilityStatement](capabilitystatement.html): Computationally friendly name of the capability statement* [Citation](citation.html): Computationally friendly name of the citation* [CodeSystem](codesystem.html): Computationally friendly name of the code system* [CompartmentDefinition](compartmentdefinition.html): Computationally friendly name of the compartment definition* [ConceptMap](conceptmap.html): Computationally friendly name of the concept map* [ConditionDefinition](conditiondefinition.html): Computationally friendly name of the condition definition* [EventDefinition](eventdefinition.html): Computationally friendly name of the event definition* [EvidenceVariable](evidencevariable.html): Computationally friendly name of the evidence variable* [ExampleScenario](examplescenario.html): Computationally friendly name of the example scenario* [GraphDefinition](graphdefinition.html): Computationally friendly name of the graph definition* [ImplementationGuide](implementationguide.html): Computationally friendly name of the implementation guide* [Library](library.html): Computationally friendly name of the library* [Measure](measure.html): Computationally friendly name of the measure* [MessageDefinition](messagedefinition.html): Computationally friendly name of the message definition* [NamingSystem](namingsystem.html): Computationally friendly name of the naming system* [OperationDefinition](operationdefinition.html): Computationally friendly name of the operation definition* [PlanDefinition](plandefinition.html): Computationally friendly name of the plan definition* [Questionnaire](questionnaire.html): Computationally friendly name of the questionnaire* [Requirements](requirements.html): Computationally friendly name of the requirements* [SearchParameter](searchparameter.html): Computationally friendly name of the search parameter* [StructureDefinition](structuredefinition.html): Computationally friendly name of the structure definition* [StructureMap](structuremap.html): Computationally friendly name of the structure map* [TerminologyCapabilities](terminologycapabilities.html): Computationally friendly name of the terminology capabilities* [TestScript](testscript.html): Computationally friendly name of the test script* [ValueSet](valueset.html): Computationally friendly name of the value set") @OptionalParam(name = "name") StringAndListParam theName,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): Name of the publisher of the activity definition* [ActorDefinition](actordefinition.html): Name of the publisher of the Actor Definition* [CapabilityStatement](capabilitystatement.html): Name of the publisher of the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): Name of the publisher of the charge item definition* [Citation](citation.html): Name of the publisher of the citation* [CodeSystem](codesystem.html): Name of the publisher of the code system* [CompartmentDefinition](compartmentdefinition.html): Name of the publisher of the compartment definition* [ConceptMap](conceptmap.html): Name of the publisher of the concept map* [ConditionDefinition](conditiondefinition.html): Name of the publisher of the condition definition* [EventDefinition](eventdefinition.html): Name of the publisher of the event definition* [Evidence](evidence.html): Name of the publisher of the evidence* [EvidenceReport](evidencereport.html): Name of the publisher of the evidence report* [EvidenceVariable](evidencevariable.html): Name of the publisher of the evidence variable* [ExampleScenario](examplescenario.html): Name of the publisher of the example scenario* [GraphDefinition](graphdefinition.html): Name of the publisher of the graph definition* [ImplementationGuide](implementationguide.html): Name of the publisher of the implementation guide* [Library](library.html): Name of the publisher of the library* [Measure](measure.html): Name of the publisher of the measure* [MessageDefinition](messagedefinition.html): Name of the publisher of the message definition* [NamingSystem](namingsystem.html): Name of the publisher of the naming system* [OperationDefinition](operationdefinition.html): Name of the publisher of the operation definition* [PlanDefinition](plandefinition.html): Name of the publisher of the plan definition* [Questionnaire](questionnaire.html): Name of the publisher of the questionnaire* [Requirements](requirements.html): Name of the publisher of the requirements* [SearchParameter](searchparameter.html): Name of the publisher of the search parameter* [StructureDefinition](structuredefinition.html): Name of the publisher of the structure definition* [StructureMap](structuremap.html): Name of the publisher of the structure map* [TerminologyCapabilities](terminologycapabilities.html): Name of the publisher of the terminology capabilities* [TestScript](testscript.html): Name of the publisher of the test script* [ValueSet](valueset.html): Name of the publisher of the value set") @OptionalParam(name = "publisher") StringAndListParam thePublisher,
											@Description(shortDefinition = "Location of the resource") @OptionalParam(name = "resource",targetTypes = {}) ReferenceAndListParam theResource,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): The current status of the activity definition* [ActorDefinition](actordefinition.html): The current status of the Actor Definition* [CapabilityStatement](capabilitystatement.html): The current status of the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): The current status of the charge item definition* [Citation](citation.html): The current status of the citation* [CodeSystem](codesystem.html): The current status of the code system* [CompartmentDefinition](compartmentdefinition.html): The current status of the compartment definition* [ConceptMap](conceptmap.html): The current status of the concept map* [ConditionDefinition](conditiondefinition.html): The current status of the condition definition* [EventDefinition](eventdefinition.html): The current status of the event definition* [Evidence](evidence.html): The current status of the evidence* [EvidenceReport](evidencereport.html): The current status of the evidence report* [EvidenceVariable](evidencevariable.html): The current status of the evidence variable* [ExampleScenario](examplescenario.html): The current status of the example scenario* [GraphDefinition](graphdefinition.html): The current status of the graph definition* [ImplementationGuide](implementationguide.html): The current status of the implementation guide* [Library](library.html): The current status of the library* [Measure](measure.html): The current status of the measure* [MedicationKnowledge](medicationknowledge.html): active | inactive | entered-in-error* [MessageDefinition](messagedefinition.html): The current status of the message definition* [NamingSystem](namingsystem.html): The current status of the naming system* [ObservationDefinition](observationdefinition.html): Publication status of the ObservationDefinition: draft, active, retired, unknown* [OperationDefinition](operationdefinition.html): The current status of the operation definition* [PlanDefinition](plandefinition.html): The current status of the plan definition* [Questionnaire](questionnaire.html): The current status of the questionnaire* [Requirements](requirements.html): The current status of the requirements* [SearchParameter](searchparameter.html): The current status of the search parameter* [SpecimenDefinition](specimendefinition.html): Publication status of the SpecimenDefinition: draft, active, retired, unknown* [StructureDefinition](structuredefinition.html): The current status of the structure definition* [StructureMap](structuremap.html): The current status of the structure map* [SubscriptionTopic](subscriptiontopic.html): draft | active | retired | unknown* [TerminologyCapabilities](terminologycapabilities.html): The current status of the terminology capabilities* [TestPlan](testplan.html): The current status of the test plan* [TestScript](testscript.html): The current status of the test script* [ValueSet](valueset.html): The current status of the value set") @OptionalParam(name = "status") TokenAndListParam theStatus,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): The human-friendly name of the activity definition* [ActorDefinition](actordefinition.html): The human-friendly name of the Actor Definition* [CapabilityStatement](capabilitystatement.html): The human-friendly name of the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): The human-friendly name of the charge item definition* [Citation](citation.html): The human-friendly name of the citation* [CodeSystem](codesystem.html): The human-friendly name of the code system* [ConceptMap](conceptmap.html): The human-friendly name of the concept map* [ConditionDefinition](conditiondefinition.html): The human-friendly name of the condition definition* [EventDefinition](eventdefinition.html): The human-friendly name of the event definition* [Evidence](evidence.html): The human-friendly name of the evidence* [EvidenceVariable](evidencevariable.html): The human-friendly name of the evidence variable* [ImplementationGuide](implementationguide.html): The human-friendly name of the implementation guide* [Library](library.html): The human-friendly name of the library* [Measure](measure.html): The human-friendly name of the measure* [MessageDefinition](messagedefinition.html): The human-friendly name of the message definition* [ObservationDefinition](observationdefinition.html): Human-friendly name of the ObservationDefinition* [OperationDefinition](operationdefinition.html): The human-friendly name of the operation definition* [PlanDefinition](plandefinition.html): The human-friendly name of the plan definition* [Questionnaire](questionnaire.html): The human-friendly name of the questionnaire* [Requirements](requirements.html): The human-friendly name of the requirements* [SpecimenDefinition](specimendefinition.html): Human-friendly name of the SpecimenDefinition* [StructureDefinition](structuredefinition.html): The human-friendly name of the structure definition* [StructureMap](structuremap.html): The human-friendly name of the structure map* [SubscriptionTopic](subscriptiontopic.html): Name for this SubscriptionTopic (Human friendly)* [TerminologyCapabilities](terminologycapabilities.html): The human-friendly name of the terminology capabilities* [TestScript](testscript.html): The human-friendly name of the test script* [ValueSet](valueset.html): The human-friendly name of the value set") @OptionalParam(name = "title") StringAndListParam theTitle,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): The uri that identifies the activity definition* [ActorDefinition](actordefinition.html): The uri that identifies the Actor Definition* [CapabilityStatement](capabilitystatement.html): The uri that identifies the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): The uri that identifies the charge item definition* [Citation](citation.html): The uri that identifies the citation* [CodeSystem](codesystem.html): The uri that identifies the code system* [CompartmentDefinition](compartmentdefinition.html): The uri that identifies the compartment definition* [ConceptMap](conceptmap.html): The URI that identifies the concept map* [ConditionDefinition](conditiondefinition.html): The uri that identifies the condition definition* [EventDefinition](eventdefinition.html): The uri that identifies the event definition* [Evidence](evidence.html): The uri that identifies the evidence* [EvidenceReport](evidencereport.html): The uri that identifies the evidence report* [EvidenceVariable](evidencevariable.html): The uri that identifies the evidence variable* [ExampleScenario](examplescenario.html): The uri that identifies the example scenario* [GraphDefinition](graphdefinition.html): The uri that identifies the graph definition* [ImplementationGuide](implementationguide.html): The uri that identifies the implementation guide* [Library](library.html): The uri that identifies the library* [Measure](measure.html): The uri that identifies the measure* [MessageDefinition](messagedefinition.html): The uri that identifies the message definition* [NamingSystem](namingsystem.html): The uri that identifies the naming system* [ObservationDefinition](observationdefinition.html): The uri that identifies the observation definition* [OperationDefinition](operationdefinition.html): The uri that identifies the operation definition* [PlanDefinition](plandefinition.html): The uri that identifies the plan definition* [Questionnaire](questionnaire.html): The uri that identifies the questionnaire* [Requirements](requirements.html): The uri that identifies the requirements* [SearchParameter](searchparameter.html): The uri that identifies the search parameter* [SpecimenDefinition](specimendefinition.html): The uri that identifies the specimen definition* [StructureDefinition](structuredefinition.html): The uri that identifies the structure definition* [StructureMap](structuremap.html): The uri that identifies the structure map* [SubscriptionTopic](subscriptiontopic.html): Logical canonical URL to reference this SubscriptionTopic (globally unique)* [TerminologyCapabilities](terminologycapabilities.html): The uri that identifies the terminology capabilities* [TestPlan](testplan.html): The uri that identifies the test plan* [TestScript](testscript.html): The uri that identifies the test script* [ValueSet](valueset.html): The uri that identifies the value set") @OptionalParam(name = "url") UriAndListParam theUrl,
											@Description(shortDefinition = "Multiple Resources: * [ActivityDefinition](activitydefinition.html): The business version of the activity definition* [ActorDefinition](actordefinition.html): The business version of the Actor Definition* [CapabilityStatement](capabilitystatement.html): The business version of the capability statement* [ChargeItemDefinition](chargeitemdefinition.html): The business version of the charge item definition* [Citation](citation.html): The business version of the citation* [CodeSystem](codesystem.html): The business version of the code system* [CompartmentDefinition](compartmentdefinition.html): The business version of the compartment definition* [ConceptMap](conceptmap.html): The business version of the concept map* [ConditionDefinition](conditiondefinition.html): The business version of the condition definition* [EventDefinition](eventdefinition.html): The business version of the event definition* [Evidence](evidence.html): The business version of the evidence* [EvidenceVariable](evidencevariable.html): The business version of the evidence variable* [ExampleScenario](examplescenario.html): The business version of the example scenario* [GraphDefinition](graphdefinition.html): The business version of the graph definition* [ImplementationGuide](implementationguide.html): The business version of the implementation guide* [Library](library.html): The business version of the library* [Measure](measure.html): The business version of the measure* [MessageDefinition](messagedefinition.html): The business version of the message definition* [NamingSystem](namingsystem.html): The business version of the naming system* [OperationDefinition](operationdefinition.html): The business version of the operation definition* [PlanDefinition](plandefinition.html): The business version of the plan definition* [Questionnaire](questionnaire.html): The business version of the questionnaire* [Requirements](requirements.html): The business version of the requirements* [SearchParameter](searchparameter.html): The business version of the search parameter* [StructureDefinition](structuredefinition.html): The business version of the structure definition* [StructureMap](structuremap.html): The business version of the structure map* [SubscriptionTopic](subscriptiontopic.html): Business version of the SubscriptionTopic* [TerminologyCapabilities](terminologycapabilities.html): The business version of the terminology capabilities* [TestScript](testscript.html): The business version of the test script* [ValueSet](valueset.html): The business version of the value set") @OptionalParam(name = "version") TokenAndListParam theVersion,
											@RawParam Map<String, List<String>> theAdditionalRawParams,
											@Description(shortDefinition = "Only return resources which were last updated as specified by the given range") @OptionalParam(name = "_lastUpdated") DateRangeParam theLastUpdated,
											@IncludeParam Set<Include> theIncludes, @IncludeParam(reverse = true) Set<Include> theRevIncludes,
											@Sort SortSpec theSort,
											@Count Integer theCount,
											@Offset Integer theOffset,
											SummaryEnum theSummaryMode,
											SearchTotalModeEnum theSearchTotalMode,
											SearchContainedModeEnum theSearchContainedMode) {
		startRequest(theServletRequest);
		try {
			List<NpmPackageVersionEntity> packages = myPackageVersionDao
					.findAll(org.springframework.data.domain.Sort.by(Direction.ASC, "myPackageId", "myVersionId"));
			List<ImplementationGuide> list = new ArrayList<ImplementationGuide>();

			for (NpmPackageVersionEntity npmPackage : packages) {
				ImplementationGuide ig = new ImplementationGuide();
				ig.setId(npmPackage.getPackageId() + "-" + npmPackage.getVersionId());
				ig.setTitle(npmPackage.getDescription());
				ig.setDate(npmPackage.getUpdatedTime());
				ig.setPackageId(npmPackage.getPackageId());
				if (npmPackage.isCurrentVersion()) {
					ig.setVersion(npmPackage.getVersionId() + " (current)");
				} else {
					ig.setVersion(npmPackage.getVersionId());
				}
				list.add(ig);
			}

			SimpleBundleProvider simpleBundleProivder = new SimpleBundleProvider(list);
			return simpleBundleProivder;

		} finally {
			endRequest(theServletRequest);
		}
	}

	@Override
	public ImplementationGuide read(HttpServletRequest theServletRequest, IIdType theId,
			RequestDetails theRequestDetails) {

		startRequest(theServletRequest);
		try {
			return new TransactionTemplate(myTxManager).execute(tx -> {
				String id = theId.getIdPart().substring(0, theId.getIdPart().lastIndexOf("-"));
				String version = theId.getIdPart().substring(theId.getIdPart().lastIndexOf("-") + 1);
				Optional<NpmPackageVersionEntity> packages = myPackageVersionDao.findByPackageIdAndVersion(id, version);
				if (packages.isPresent()) {
					NpmPackageVersionEntity npmPackage = packages.get();
					ImplementationGuide ig = new ImplementationGuide();
					ig.setId(npmPackage.getPackageId() + "-" + npmPackage.getVersionId());
					ig.setTitle(npmPackage.getDescription());
					ig.setDate(npmPackage.getUpdatedTime());
					ig.setPackageId(npmPackage.getPackageId());
					ig.setVersion(npmPackage.getVersionId());
					return ig;
				}
				return null;
			});
		} finally {
			endRequest(theServletRequest);
		}
	}

}
