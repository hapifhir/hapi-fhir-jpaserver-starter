package ch.ahdis.matchbox.providers;

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
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.engine.cli.VersionUtil;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import ch.ahdis.matchbox.questionnaire.QuestionnaireResponseExtractProvider;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseConformance;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IIdType;
import org.hl7.fhir.r5.model.*;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import static ch.ahdis.matchbox.packages.MatchboxJpaPackageCache.structureDefinitionIsValidatable;

/**
 * A provider of CapabilityStatement customized for Matchbox.
 */
public class MatchboxCapabilityStatementProvider extends ServerCapabilityStatementProvider {
	private static final String VALIDATE_OPERATION_NAME = "Validate";
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
					new org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent(new org.hl7.fhir.r4.model.Enumeration<>(
						new org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteractionEnumFactory(),
						org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteraction.READ));
				interactionSearch =
					new org.hl7.fhir.r4.model.CapabilityStatement.ResourceInteractionComponent(new org.hl7.fhir.r4.model.Enumeration<>(
						new org.hl7.fhir.r4.model.CapabilityStatement.TypeRestfulInteractionEnumFactory(),
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
	public IBaseResource readOperationDefinition(@IdParam final IIdType theId,
																final RequestDetails theRequestDetails) {
		final var baseResource = super.readOperationDefinition(theId, theRequestDetails);

		final Consumer<OperationDefinition> updateOperationDefinition = opDefR5 -> {
			switch (opDefR5.getName()) {
				case VALIDATE_OPERATION_NAME -> this.updateValidateOperationDefinition(opDefR5);
				case QuestionnaireResponseExtractProvider.OPERATION_NAME -> QuestionnaireResponseExtractProvider.updateOperationDefinition(opDefR5);
				default -> {
					// Do nothing
				}
			}
		};

		if (baseResource instanceof final OperationDefinition opDefR5) {
			// In R5 mode
			updateOperationDefinition.accept(opDefR5);
			return baseResource;
		} else if (baseResource instanceof final org.hl7.fhir.r4b.model.OperationDefinition opDefR4B) {
			// In R4B mode: convert to R5, update, and convert back to R4B
			final var opDefR5 = (OperationDefinition) VersionConvertorFactory_43_50.convertResource(opDefR4B);
			updateOperationDefinition.accept(opDefR5);
			return VersionConvertorFactory_43_50.convertResource(opDefR5);
		} else if (baseResource instanceof final org.hl7.fhir.r4.model.OperationDefinition opDefR4) {
			// In R4 mode: convert to R5, update, and convert back to R4
			final var opDefR5 = (OperationDefinition) VersionConvertorFactory_40_50.convertResource(opDefR4);
			updateOperationDefinition.accept(opDefR5);
			return VersionConvertorFactory_40_50.convertResource(opDefR5);
		}
		// Only fail if the base resource is not R4, R4B or R5
		throw new MatchboxUnsupportedFhirVersionException("MatchboxCapabilityStatementProvider",
																		  baseResource.getStructureFhirVersionEnum());
	}

	/**
	 * Updates an R5 OperationDefinition with the parameters required for the $validate operation, including the
	 * parameters supported and the list of installed profiles.
	 */
	private void updateValidateOperationDefinition(final OperationDefinition validateOperationDefinition) {
		validateOperationDefinition.addParameter()
			.setName("resource")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.RESOURCE);
		validateOperationDefinition.addParameter()
			.setName("mode")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.CODE);

		final var profiles = this.structureDefinitionProvider.getCanonicalsR5().stream()
			.filter(sd -> structureDefinitionIsValidatable(sd.getExtensionByUrl("sd-title").getValueStringType().getValue()))
			.toList();
		validateOperationDefinition.addParameter()
			.setName("profile")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.CANONICAL)
			.setTargetProfile(profiles);
		validateOperationDefinition.addParameter()
			.setName("reload")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.BOOLEAN);

		final var cliContextProperties = this.cliContext.getValidateEngineParameters();
		for (final Field field : cliContextProperties) {
			field.setAccessible(true);
			try {
				validateOperationDefinition.addParameter()
					.setName(field.getName())
					.setUse(Enumerations.OperationParameterUse.IN)
					.setMin(0)
					.setMax("1")
					.setType(field.getType().equals(boolean.class) || field.getType().equals(Boolean.class) ? Enumerations.FHIRTypes.BOOLEAN : Enumerations.FHIRTypes.STRING)
					.addExtension("http://matchbox.health/validationDefaultValue",
									  field.getType().equals(boolean.class) || field.getType().equals(Boolean.class) ? new BooleanType(
										  (Boolean) field.get(cliContext)) : new StringType((String) field.get(cliContext)));
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		validateOperationDefinition.addParameter()
			.setName("extensions")
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setType(Enumerations.FHIRTypes.STRING);
	}
}
