package ch.ahdis.matchbox.validation;

import ca.uhn.fhir.rest.api.EncodingEnum;
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.List;

/**
 * The service that runs FHIR validations.
 */
public class McpValidationService {
	private static final Logger log = LoggerFactory.getLogger(McpValidationService.class);

	private final MatchboxEngineSupport matchboxEngineSupport;
	private final CliContext cliContext;

	public McpValidationService(final MatchboxEngineSupport matchboxEngineSupport,
										 final CliContext cliContext) {
		this.matchboxEngineSupport = matchboxEngineSupport;
		this.cliContext = cliContext;
	}

	/**
	 * This method is the entry point for the MCP tool.
	 */
	@Tool(name = "validateResource", description = "Validate a FHIR resource against a profile")
	public List<ValidationMessage> mcpToolGetValidation(@ToolParam(description = "The FHIR resource to validate (JSON or XML)") final String resource,
												  @ToolParam(description = "The FHIR profile to use") final String profile) {
		// 1. Get a Matchbox engine for the given profile
		final MatchboxEngine engine;
		try {
			engine = this.matchboxEngineSupport.getMatchboxEngine(profile, cliContext, true, false);
		} catch (final Exception e) {
			log.error("Error while initializing the validation engine", e);
			return List.of(new ValidationMessage()
				.setLevel(ValidationMessage.IssueSeverity.ERROR)
				.setMessage("Error while initializing the validation engine: %s".formatted(e.getMessage()))
			);
		}
		if (engine == null) {
			return List.of(new ValidationMessage()
				.setLevel(ValidationMessage.IssueSeverity.ERROR)
				.setMessage("Matchbox engine for profile '%s' could not be created, check the installed IGs".formatted(
					profile)));
		}

		// 2. Run the validation
		final var encoding = EncodingEnum.detectEncoding(resource);
		final List<ValidationMessage> messages;
		try {
			messages = ValidationProvider.doValidate(engine, resource, encoding, profile);
		} catch (final Exception e) {
			log.error("Error during validation", e);
			return List.of(new ValidationMessage()
				.setLevel(ValidationMessage.IssueSeverity.ERROR)
				.setMessage("Error during validation: %s".formatted(e.getMessage()))
			);
		}

		return messages;
	}
}
