package ch.ahdis.matchbox;

import java.lang.reflect.Field;
import java.util.List;

import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent;
import org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction;
import org.hl7.fhir.r4.model.OperationDefinition.OperationDefinitionParameterComponent;
import org.hl7.fhir.r4.model.OperationDefinition.OperationParameterUse;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ServerCapabilityStatementProvider;
import ca.uhn.fhir.util.FhirTerser;
import ca.uhn.fhir.util.TerserUtil;
import ch.ahdis.matchbox.engine.cli.VersionUtil;

public class MatchboxCapabilityStatementProvider extends ServerCapabilityStatementProvider {

	
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MatchboxCapabilityStatementProvider.class);
	private StructureDefinitionResourceProvider structureDefinitionProvider;

	protected CliContext cliContext;
	protected FhirContext myFhirContext;


	public MatchboxCapabilityStatementProvider(FhirContext fhirContext, RestfulServer theServerConfiguration, StructureDefinitionResourceProvider structureDefinitionProvider, CliContext cliContext) {
		super(theServerConfiguration, null, null);
		this.structureDefinitionProvider = structureDefinitionProvider;
		this.cliContext = cliContext;
		theServerConfiguration.setServerName(VersionUtil.getPoweredBy());
		theServerConfiguration.setServerVersion(VersionUtil.getVersion());
		if (cliContext.getOnlyOneEngine()) {
			theServerConfiguration.setImplementationDescription("Development mode");
		}
		this.myFhirContext = fhirContext;
	}

	protected void postProcessRestResource(FhirTerser theTerser, IBase theResource, String theResourceName) {
	}

	private static void setField(
		FhirContext theFhirContext,
		FhirTerser theTerser,
		String theFieldName,
		IBase theBase,
		IBase... theValues) {
		BaseRuntimeElementDefinition definition = theFhirContext.getElementDefinition(theBase.getClass());
		BaseRuntimeChildDefinition childDefinition = definition.getChildByName(theFieldName);
		for (IBase value : theValues) {
			try {
				childDefinition.getMutator().addValue(theBase, value);
			} catch (UnsupportedOperationException e) {
				childDefinition.getMutator().setValue(theBase, value);
				break;
			}
		}
		return;
	}

	/**
	 * We need to clean up the default capability statement, in development mode we allow update and create on all conformance resources
	 * otherwise just read access
	 */
	protected void postProcess(FhirTerser theTerser, IBaseConformance theCapabilityStatement) {
		ResourceInteractionComponent interaction = new ResourceInteractionComponent();
		ResourceInteractionComponent interactionSearch = new ResourceInteractionComponent();
		interaction.setCode(TypeRestfulInteraction.READ);
		interactionSearch.setCode(TypeRestfulInteraction.SEARCHTYPE);
		List<IBase> resources = TerserUtil.getFieldByFhirPath(this.myFhirContext, "rest.resource", theCapabilityStatement);
		for (IBase resource : resources) {
			StringType stringType = (StringType) TerserUtil.getFirstFieldByFhirPath(this.myFhirContext, "type", resource);
			if (stringType!=null && !"ImplementationGuide".equals(stringType.getValueNotNull())) {
				if (cliContext.getOnlyOneEngine() == false) {
					TerserUtil.clearField(myFhirContext, "interaction", resource);
					if (stringType!=null && !"QuestionnaireResponse".equals(stringType.getValueNotNull())) {
						setField(myFhirContext,theTerser, "interaction", resource, interaction, interactionSearch);
					}
				}
				TerserUtil.clearField(myFhirContext, "searchRevInclude", resource);
				TerserUtil.clearField(myFhirContext, "searchInclude", resource);
				IBase value = TerserUtil.newElement(myFhirContext, "boolean", "false");
				setField(myFhirContext,theTerser, "conditionalCreate", resource, value);
				setField(myFhirContext,theTerser, "conditionalUpdate", resource, value);
			}
		}
	}

	@Read(typeName = "OperationDefinition")
	@Override
	public IBaseResource readOperationDefinition(@IdParam IIdType theId, RequestDetails theRequestDetails) {
		
		org.hl7.fhir.r4.model.OperationDefinition operationDefintion = (org.hl7.fhir.r4.model.OperationDefinition) super.readOperationDefinition(theId, theRequestDetails);
		if ("Validate".equals(operationDefintion.getName())) {
			ourLog.info("adding profiles to $validate");
//	existing:	  "parameter": [
//		                {
//		                  "name": "return",
//		                  "use": "out",
//		                  "min": 1,
//		                  "max": "1"
//		                }
//		              ]
			OperationDefinitionParameterComponent parameter = operationDefintion.addParameter();
			parameter.setName("resource").setUse(OperationParameterUse.IN).setMin(0).setMax("1").setType("Resource");
			parameter = operationDefintion.addParameter();
			parameter.setName("mode").setUse(OperationParameterUse.IN).setMin(0).setMax("1").setType("code");
			parameter = operationDefintion.addParameter();
			parameter.setName("profile").setUse(OperationParameterUse.IN).setMin(0).setMax("1").setType("canonical");
			parameter.setTargetProfile(structureDefinitionProvider.getCanonicals());
			parameter = operationDefintion.addParameter();
			parameter.setName("reload").setUse(OperationParameterUse.IN).setMin(0).setMax("1").setType("boolean");

			List<Field> cliContextProperties = cliContext.getValidateEngineParameters();
			for (Field field : cliContextProperties) {
				parameter = operationDefintion.addParameter();
				parameter.setName(field.getName()).setUse(OperationParameterUse.IN).setMin(0).setMax("1").setType(field.getType().equals(boolean.class) ? "boolean" : "string");
			}
		}
		return operationDefintion;
	}


}
