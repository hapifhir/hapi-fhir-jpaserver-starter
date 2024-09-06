package ch.ahdis.matchbox;

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
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.CapabilityStatement;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.OperationDefinition;
import org.hl7.fhir.r5.model.StringType;

import java.lang.reflect.Field;

import static ch.ahdis.matchbox.util.MatchboxPackageInstallerImpl.SD_EXTENSION_TITLE_PREFIX;

/**
 * A provider of CapabilityStatement customized for Matchbox.
 */
public class MatchboxCapabilityStatementProvider extends ServerCapabilityStatementProvider {
	private final StructureDefinitionResourceProvider structureDefinitionProvider;
	protected final CliContext cliContext;
	protected final FhirContext myFhirContext;

	public MatchboxCapabilityStatementProvider(final FhirContext fhirContext,
															 final RestfulServer theServerConfiguration,
															 final StructureDefinitionResourceProvider structureDefinitionProvider,
															 final CliContext cliContext) {
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
	 * We need to clean up the default capability statement, in development mode we allow update and create on all
	 * conformance resources otherwise just read access
	 */
	@Override
	protected void postProcess(FhirTerser theTerser, IBaseConformance theCapabilityStatement) {
		final var resources = TerserUtil.getFieldByFhirPath(this.myFhirContext, "rest.resource", theCapabilityStatement);
		for (final IBase resource : resources) {
			final var baseType = TerserUtil.getFirstFieldByFhirPath(this.myFhirContext, "type", resource);
			final String type;
			final IBase interaction;
			final IBase interactionSearch;
			if (baseType instanceof final StringType stringTypeR5) {
				type = stringTypeR5.getValueNotNull();
				interaction = new CapabilityStatement.ResourceInteractionComponent(CapabilityStatement.TypeRestfulInteraction.READ);
				interactionSearch = new CapabilityStatement.ResourceInteractionComponent(CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
			} else if (baseType instanceof final org.hl7.fhir.r4.model.StringType stringTypeR4) {
				type = stringTypeR4.getValueNotNull();
				interaction =
					new org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent(new org.hl7.fhir.r4.model.Enumeration<>(new org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteractionEnumFactory(),
																																				org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction.READ));
				interactionSearch =
					new org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent(new org.hl7.fhir.r4.model.Enumeration<>(new org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteractionEnumFactory(),
																																				org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE));
			} else if (baseType instanceof final org.hl7.fhir.r4b.model.StringType stringTypeR4B) {
				type = stringTypeR4B.getValueNotNull();
				interaction =
					new org.hl7.fhir.r4b.model.CapabilityStatement.ResourceInteractionComponent(org.hl7.fhir.r4b.model.CapabilityStatement.TypeRestfulInteraction.READ);
				interactionSearch =
					new org.hl7.fhir.r4b.model.CapabilityStatement.ResourceInteractionComponent(org.hl7.fhir.r4b.model.CapabilityStatement.TypeRestfulInteraction.SEARCHTYPE);
			} else {
				throw new MatchboxUnsupportedFhirVersionException("MatchboxCapabilityStatementProvider",
																				  this.myFhirContext.getVersion().getVersion());
			}

			if (!"ImplementationGuide".equals(type)) {
				if (!cliContext.getOnlyOneEngine()) {
					TerserUtil.clearField(myFhirContext, "interaction", resource);
					if (!"QuestionnaireResponse".equals(type)) {
						setField(myFhirContext, theTerser, "interaction", resource, interaction, interactionSearch);
					}
				}
				TerserUtil.clearField(myFhirContext, "searchRevInclude", resource);
				TerserUtil.clearField(myFhirContext, "searchInclude", resource);
				IBase value = TerserUtil.newElement(myFhirContext, "boolean", "false");
				setField(myFhirContext, theTerser, "conditionalCreate", resource, value);
				setField(myFhirContext, theTerser, "conditionalUpdate", resource, value);
			}
		}
	}

	/**
	 * A hook on the read operation definition method to update $validate with its parameters.
	 */
	@Read(typeName = "OperationDefinition")
	@Override
	public IBaseResource readOperationDefinition(@IdParam IIdType theId, RequestDetails theRequestDetails) {
		final var baseResource = super.readOperationDefinition(theId, theRequestDetails);
		if (baseResource instanceof final OperationDefinition opDefR5 && "Validate".equals(opDefR5.getName())) {
			// In R5 mode
			this.updateOperationDefinition(opDefR5);
			return baseResource;
		} else if (baseResource instanceof final org.hl7.fhir.r4b.model.OperationDefinition opDefR4B && "Validate".equals(
			opDefR4B.getName())) {
			// In R4B mode: convert to R5, update, and convert back to R4B
			final var opDefR5 = (OperationDefinition) VersionConvertorFactory_43_50.convertResource(opDefR4B);
			this.updateOperationDefinition(opDefR5);
			return VersionConvertorFactory_43_50.convertResource(opDefR5);
		} else if (baseResource instanceof final org.hl7.fhir.r4.model.OperationDefinition opDefR4 && "Validate".equals(
			opDefR4.getName())) {
			// In R4 mode: convert to R5, update, and convert back to R4
			final var opDefR5 = (OperationDefinition) VersionConvertorFactory_40_50.convertResource(opDefR4);
			this.updateOperationDefinition(opDefR5);
			return VersionConvertorFactory_40_50.convertResource(opDefR5);
		}
		throw new MatchboxUnsupportedFhirVersionException("MatchboxCapabilityStatementProvider",
																		  theRequestDetails.getFhirContext().getVersion().getVersion());
	}

	/**
	 * Updates an R5 OperationDefinition with the parameters required for the $validate operation, including the
	 * parameters supported and the list of installed profiles.
	 */
	private void updateOperationDefinition(final OperationDefinition operationDefinition) {
		operationDefinition.addParameter()
			.setName("resource")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.RESOURCE);
		operationDefinition.addParameter()
			.setName("mode")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.CODE);

		final var profiles = this.structureDefinitionProvider.getCanonicalsR5().stream()
			.filter(sd -> !sd.getExtensionByUrl("sd-title").getValueStringType().getValue().startsWith(
				SD_EXTENSION_TITLE_PREFIX))
			.toList();
		operationDefinition.addParameter()
			.setName("profile")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.CANONICAL)
			.setTargetProfile(profiles);
		operationDefinition.addParameter()
			.setName("reload")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.BOOLEAN);

		final var cliContextProperties = this.cliContext.getValidateEngineParameters();
		for (final Field field : cliContextProperties) {
			operationDefinition.addParameter()
				.setName(field.getName())
				.setUse(Enumerations.OperationParameterUse.IN)
				.setMin(0)
				.setMax("1")
				.setType(field.getType().equals(boolean.class) ? Enumerations.FHIRTypes.BOOLEAN : Enumerations.FHIRTypes.STRING);
		}

		operationDefinition.addParameter()
			.setName("extensions")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.STRING);

	}
}
