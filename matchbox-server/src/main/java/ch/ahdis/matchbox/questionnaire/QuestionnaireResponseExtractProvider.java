package ch.ahdis.matchbox.questionnaire;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.util.http.HttpRequestWrapper;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hl7.fhir.r5.elementmodel.*;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;

/**
 * $extract Operation for QuestionnaireResponse Resources
 */
public class QuestionnaireResponseExtractProvider {
	protected static final Logger log =
		LoggerFactory.getLogger(QuestionnaireResponseExtractProvider.class);

	public static final String OPERATION_NAME = "Extract";
	public static final String PARAM_IN_QUESTIONNAIRE_RESPONSE = "questionnaire-response";
	public static final String PARAM_IN_QUESTIONNAIRE = "questionnaire";
	public static final String PARAM_IN_STRUCTURE_MAP = "structure-map";
	public static final String PARAM_OUT_RETURN = "return";
	public static final String PARAM_OUT_ISSUES = "issues";

	public static final String TARGET_STRUCTURE_MAP = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-targetStructureMap";

	private final MatchboxEngineSupport matchboxEngineSupport;

	public QuestionnaireResponseExtractProvider(final MatchboxEngineSupport matchboxEngineSupport) {
		this.matchboxEngineSupport = matchboxEngineSupport;
	}

	public void extract(final HttpServletRequest theServletRequest,
							  final HttpServletResponse theServletResponse) throws IOException {
		final var httpWrapper = this.matchboxEngineSupport.createWrapper(theServletRequest, theServletResponse);

		final var parsedRequest = this.parseRequest(httpWrapper);
		final String questionnaireUri = parsedRequest.questionnaireResponse().getQuestionnaire();
		if (questionnaireUri == null) {
			throw new UnprocessableEntityException("No questionnaire canonical URL given.");
		}

		final MatchboxEngine matchboxEngine;
		final Questionnaire questionnaire;
		if (parsedRequest.questionnaire() != null) {
			if (!questionnaireUri.equals(parsedRequest.questionnaire().getUrl())) {
				throw new UnprocessableEntityException(
					"Questionnaire canonical URL in QuestionnaireResponse does not match questionnaire given in parameters");
			}

			// Create a new engine
			matchboxEngine = this.matchboxEngineSupport.getMatchboxEngine(null, null, true, false);
			if (matchboxEngine == null) {
				throw new UnprocessableEntityException(
					"Could not initialize a new matchbox-engine");
			}

			questionnaire = parsedRequest.questionnaire();
		} else {
			matchboxEngine = this.matchboxEngineSupport.getMatchboxEngine(questionnaireUri, null, true, false);
			if (matchboxEngine == null) {
				throw new UnprocessableEntityException(
					"Could not get matchbox-engine with questionnaire with canonical URL '" + questionnaireUri + "'");
			}

			// Let's find the Questionnaire in the engine
			questionnaire = (Questionnaire) matchboxEngine.getCanonicalResource(questionnaireUri, "5.0.0");
			if (questionnaire == null) {
				throw new UnprocessableEntityException(
					"Could not fetch questionnaire with canonical URL '" + questionnaireUri + "'");
			}
		}

		// Get targetStructureMap extension from questionnaire
		final Extension targetStructureMapExtension = questionnaire.getExtensionByUrl(TARGET_STRUCTURE_MAP);
		if (targetStructureMapExtension == null) {
			throw new UnprocessableEntityException("No sdc-questionnaire-targetStructureMap extension found in resource");
		}
		final String mapUrl = targetStructureMapExtension.getValue().primitiveValue();

		if (parsedRequest.structureMap() != null) {
			matchboxEngine.getContext().cacheResource(parsedRequest.structureMap());
		}

		final var objectConverter = new ObjectConverter(matchboxEngine.getContext());
		final var questionnaireResponseElement = objectConverter.convert(parsedRequest.questionnaireResponse());
		final Element result = matchboxEngine.transform(questionnaireResponseElement, mapUrl, null);

		httpWrapper.writeResponse(objectConverter.convert(result));
	}

	/**
	 * Parse the $extract request.
	 */
	private ExtractRequest parseRequest(final HttpRequestWrapper httpWrapper) {
		switch (httpWrapper.parseBodyAsResource()) {
			case Parameters parameters -> {
				final var questionnaireResponse = this.getParameterResourceByName(parameters,
																										PARAM_IN_QUESTIONNAIRE_RESPONSE,
																										QuestionnaireResponse.class);
				final var questionnaire = this.getParameterResourceByName(parameters,
																							 PARAM_IN_QUESTIONNAIRE,
																							 Questionnaire.class);

				if (questionnaireResponse == null) {
					throw new UnprocessableEntityException(
						"Missing QuestionnaireResponse resource in parameter 'questionnaire-response' of $extract operation");
				}

				// Try to extract a StructureMap from the Parameters (as a resource or as an FML string)
				StructureMap structureMap = this.getParameterResourceByName(parameters,
																								PARAM_IN_STRUCTURE_MAP,
																								StructureMap.class);
				if (structureMap == null && parameters.getParameterValue(PARAM_IN_STRUCTURE_MAP) instanceof final StringType fml) {
					structureMap = this.parseFml(fml.getValueNotNull());
				}

				return new ExtractRequest(questionnaireResponse, questionnaire, structureMap);
			}
			case QuestionnaireResponse questionnaireResponse -> {
				return new ExtractRequest(questionnaireResponse, null, null);
			}
			case null, default -> throw new UnprocessableEntityException(
					"Invalid body resource type for $extract operation. Expected 'Parameters' or 'QuestionnaireResponse'");
		}
	}

	/**
	 * A helper method to get a resource from the parameters.
	 */
	@Nullable
	private <T extends Resource> T getParameterResourceByName(final Parameters parameters,
															 final String name,
															 final Class<T> expectedResourceType) {
		return Optional.ofNullable(parameters.getParameter(name))
			.map(Parameters.ParametersParameterComponent::getResource)
			.filter(expectedResourceType::isInstance)
			.map(expectedResourceType::cast)
			.orElse(null);
	}

	/**
	 * Parse an R5 StructureMap from a FML string.
	 */
	private StructureMap parseFml(final String fml) {
		try {
			final var cliContext = new CliContext(this.matchboxEngineSupport.getClientContext());
			cliContext.setFhirVersion(FhirVersionEnum.R5.getFhirVersionString());
			final var tempEngine = this.matchboxEngineSupport.getMatchboxEngine(null, cliContext, true, false);
			return tempEngine.parseMapR5(fml);
		} catch (final Exception e) {
			throw new InvalidRequestException("Unable to parse the FML language", e);
		}
	}

	/**
	 * A parsed request for the $extract operation.
	 *
	 * @param questionnaireResponse The questionnaire response to extract from
	 * @param questionnaire         The questionnaire definition, if unknown from the server
	 */
	record ExtractRequest(QuestionnaireResponse questionnaireResponse,
								 @Nullable Questionnaire questionnaire,
								 @Nullable StructureMap structureMap) {
	}

	/**
	 * Updates an R5 OperationDefinition with the parameters required for the $extract operation.
	 * Based on https://build.fhir.org/ig/HL7/sdc/OperationDefinition-QuestionnaireResponse-extract.html
	 */
	public static void updateOperationDefinition(final OperationDefinition extractOperationDefinition) {
		extractOperationDefinition.setDescription(
			"""
				The Extract operation takes a completed QuestionnaireResponse and  converts it to a FHIR resource or \
				Bundle of resources by using metadata embedded in the Questionnaire the QuestionnaireResponse is based on. \
				The extracted resources might include Observations, MedicationStatements and other standard FHIR resources \
				which can then be shared and manipulated. When invoking the $extract operation, care should be taken that \
				the submitted QuestionnaireResponse is itself valid. If not, the extract operation could fail (with \
				appropriate OperationOutcomes) or, more problematic, might succeed but provide incorrect output.""");
		extractOperationDefinition.setComment("The QuestionnaireResponse must identify a Questionnaire instance " +
															  "containing appropriate metadata to allow extraction.");

		extractOperationDefinition.addParameter()
			.setName(PARAM_IN_QUESTIONNAIRE_RESPONSE)
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(1) // required because we only support the operation on type, not on instance
			.setMax("1")
			.setDocumentation("The QuestionnaireResponse to extract data from.")
			.setType(Enumerations.FHIRTypes.QUESTIONNAIRERESPONSE);
		extractOperationDefinition.addParameter()
			.setName(PARAM_IN_QUESTIONNAIRE)
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setDocumentation("Matchbox extension. The Questionnaire resource, if unknown from the server.")
			.setType(Enumerations.FHIRTypes.QUESTIONNAIRE);
		extractOperationDefinition.addParameter()
			.setName(PARAM_IN_STRUCTURE_MAP)
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setDocumentation("Matchbox extension. The StructureMap resource, if unknown from the server.")
			.setType(Enumerations.FHIRTypes.STRUCTUREMAP)
			.addAllowedType(Enumerations.FHIRTypes.STRING);
		extractOperationDefinition.addParameter()
			.setName(PARAM_IN_STRUCTURE_MAP)
			.setUse(Enumerations.OperationParameterUse.IN)
			.setMin(0)
			.setMax("1")
			.setDocumentation("Matchbox extension. The StructureMap as a FML string, if unknown from the server.")
			.setType(Enumerations.FHIRTypes.STRING);

		extractOperationDefinition.addParameter()
			.setName(PARAM_OUT_RETURN)
			.setUse(Enumerations.OperationParameterUse.OUT)
			.setMin(0)
			.setMax("1")
			.setDocumentation(
				"""
					The resulting FHIR resource produced after extracting data. This will either be a single resource or a \
					Transaction Bundle that contains multiple resources.  The operations in the Bundle might be creates, \
					updates and/or conditional versions of both depending on the nature of the extraction mappings.""")
			.setType(Enumerations.FHIRTypes.RESOURCE);
		extractOperationDefinition.addParameter()
			.setName(PARAM_OUT_ISSUES)
			.setUse(Enumerations.OperationParameterUse.OUT)
			.setMin(0)
			.setMax("1")
			.setDocumentation(
				"""
					A list of hints and warnings about problems encountered while extracting the resource(s) from the \
					QuestionnaireResponse. If there was nothing to extract, a 'success' OperationOutcome is returned with \
					a warning and/or information messages. In situations where the input is invalid or the operation \
					otherwise fails to complete successfully, a normal 'erroneous' OperationOutcome would be returned (as \
					happens with all operations) indicating what the issue was.""")
			.setType(Enumerations.FHIRTypes.OPERATIONOUTCOME);
	}
}
