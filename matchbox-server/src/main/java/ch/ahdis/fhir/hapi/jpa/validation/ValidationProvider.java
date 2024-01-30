package ch.ahdis.fhir.hapi.jpa.validation;

/*
 * #%L
 * Matchbox Server
 * %%
 * Copyright (C) 2018 - 2020 ahdis
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.util.StopWatch;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.MatchboxEngineSupport;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.cli.VersionUtil;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.model.Duration;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.StringType;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.utils.OperationOutcomeUtilities;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Operation $validate
 */
public class ValidationProvider {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationProvider.class);

	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;

	@Autowired
	protected CliContext cliContext;

	@Autowired
	private FhirContext myContext;

//	@Operation(name = "$canonical", manualRequest = true, idempotent = true, returnParameters = {
//			@OperationParam(name = "return", type = IBase.class, min = 1, max = 1) })
//	public IBaseResource canonical(HttpServletRequest theRequest) {
//    String contentString = getContentString(theRequest, null);
//    EncodingEnum encoding = EncodingEnum.forContentType(theRequest.getContentType());
//    if (encoding == null) {
//      encoding = EncodingEnum.detectEncoding(contentString);
//    }
//    IBaseResource resource = null;
//    try {
//      // we still parse to catch wrongli formatted
//      resource = encoding.newParser(myFhirCtx).parseResource(contentString);
//      Canonicalizer canonicalizer= new Canonicalizer(this.myFhirCtx);
//      return canonicalizer.canonicalize(resource);
//    } catch (DataFormatException e) {
//      return getValidationMessageDataFormatException(e);
//    }
//		return null;
//	}

	@Operation(name = "$validate", manualRequest = true, idempotent = true, returnParameters = {
		@OperationParam(name = "return", type = IBase.class, min = 1, max = 1)})
	public IBaseResource validate(final HttpServletRequest theRequest) {
		log.debug("$validate");
		final ArrayList<SingleValidationMessage> addedValidationMessages = new ArrayList<>();

		final var sw = new StopWatch();
		sw.startTask("Total");

		// we extract here all config
		final CliContext cliContext = new CliContext(this.cliContext);

		// get al list of all JsonProperty of cliContext with return values property name and property type
		List<Field> cliContextProperties = cliContext.getValidateEngineParameters();

		// check for each cliContextProperties if it is in the request parameter
		for (final Field field : cliContextProperties) {
			final String cliContextProperty = field.getName();
			if (theRequest.getParameter(cliContextProperty) != null) {
				try {
					final String value = theRequest.getParameter(cliContextProperty);
					// currently only handles boolean or String
					if (field.getType() == boolean.class) {
						BeanUtils.setProperty(cliContext, cliContextProperty, Boolean.parseBoolean(value));
					} else {
						BeanUtils.setProperty(cliContext, cliContextProperty, value);
					}
				} catch (final IllegalAccessException | InvocationTargetException e) {
					log.error("error setting property " + cliContextProperty + " to " + theRequest.getParameter(
						cliContextProperty));
				}
			}
		}

		if (theRequest.getParameter("profile") == null) {
			return this.getOoForError("The 'profile' parameter must be provided");
		}
		final String profile = theRequest.getParameter("profile");

		boolean reload = false;
		if (theRequest.getParameter("reload") != null) {
			reload = theRequest.getParameter("reload").equals("true");
		}

		String contentString = "";
		try {
			contentString = new String(theRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
		} catch (final Exception e) {
			log.error(e.getMessage(), e);
		}

		if (contentString.isEmpty()) {
			return this.getOoForError("No content provided in HTTP body");
		} else {
			log.trace(contentString);
		}

		final MatchboxEngine engine;
		try {
			engine = this.matchboxEngineSupport.getMatchboxEngine(profile, cliContext, true, reload);
		} catch (final Exception e) {
			log.error("Error while initializing the validation engine", e);
			return this.getOoForError("Error while initializing the validation engine: %s".formatted(e.getMessage()));
		}
		if (engine == null || engine.getStructureDefinition(profile) == null) {
			return this.getOoForError(
				"Validation for profile '%s' not supported by this server, but additional ig's could be configured.".formatted(
					profile));
		}
		if (!this.matchboxEngineSupport.isInitialized()) {
			return this.getOoForError("Validation engine not initialized, please try again");
		}

		final String sha3Hex = new DigestUtils("SHA3-256").digestAsHex(contentString + profile);

		EncodingEnum encoding = EncodingEnum.forContentType(theRequest.getContentType());
		if (encoding == null) {
			encoding = EncodingEnum.detectEncoding(contentString);
		}

		final List<ValidationMessage> messages;
		try {
			messages = doValidate(engine, contentString, encoding, profile);
		} catch (final Exception e) {
			sw.endCurrentTask();
			log.debug("Validation time: {}", sw);
			log.error("Error during validation", e);
			return this.getOoForError("Error during validation: %s".formatted(e.getMessage()));
		}

		long millis = sw.getMillis();
		log.debug("Validation time: {}", sw);

		return this.getOperationOutcome(sha3Hex, messages, profile, engine, millis, cliContext);
	}


	private IBaseResource getOperationOutcome(final String id,
															final List<ValidationMessage> messages,
															final String profile,
															final MatchboxEngine engine,
															final long ms,
															final CliContext cliContext) {
		final var oo = new OperationOutcome();
		oo.setId(id);

		{
			// Add an information message about the validation
			final var issue = oo.addIssue();
			issue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
			issue.setCode(OperationOutcome.IssueType.INFORMATIONAL);

			final StructureDefinition structDef = engine.getStructureDefinition(profile);
			final org.hl7.fhir.r5.model.StructureDefinition structDefR5 = (org.hl7.fhir.r5.model.StructureDefinition) VersionConvertorFactory_40_50.convertResource(structDef);

			final var profileDate = (structDef.getDateElement() != null)
				? " (%s)".formatted(structDef.getDateElement().asStringValue())
				: " ";

			issue.setDiagnostics(
				"Validation for profile %s|%s%s. Loaded packages: %s. Duration: %s. %s. Validation parameters: %s".formatted(
					structDef.getUrl(),
					structDef.getVersion(),
					profileDate,
					String.join(", ", engine.getContext().getLoadedPackages()),
					ms/1000.0+ "s",
					VersionUtil.getPoweredBy(),
					cliContext.toString()
				));

			var ext = issue.addExtension().setUrl("http://matchbox.health/validiation");
			ext.addExtension("profile", new UriType(structDef.getUrl()));
			ext.addExtension("profileVersion", new UriType(structDef.getVersion()));
			ext.addExtension("profileDate", structDefR5.getDateElement());

			ext.addExtension("total", new Duration().setUnit("ms").setValue(ms) );
			ext.addExtension("validatorVersion", new StringType(VersionUtil.getPoweredBy()));
			cliContext.addContextToExtension(ext);
			if (matchboxEngineSupport.getSessionId(engine) != null) {
				ext.addExtension("sessionId", new StringType(matchboxEngineSupport.getSessionId(engine)));
			}
			for(String pkg : engine.getContext().getLoadedPackages()) {
				ext.addExtension("package", new StringType(pkg));
			}

		}

		// Map the SingleValidationMessages to OperationOutcomeIssue
		for (final ValidationMessage message : messages) {
			if (message.getType() == null) {
				// TODO: this did not happen with other core versions
				message.setType(ValidationMessage.IssueType.UNKNOWN);
			}
			final var issue = OperationOutcomeUtilities.convertToIssue(message, oo);

			// Note: the message is mapped to details.text by HAPI, but we still need it in diagnostics for the EVSClient,
			//       so we move it. This could be changed in the future.
			issue.setDiagnostics(message.getMessage());
			issue.setDetails(null);

			// Add slice info to diagnostics
			if (message.sliceText != null) {
				final var newDiagnostics = new StringBuilder();
				newDiagnostics.append(issue.getDiagnostics());
				newDiagnostics.append(" Slice info:");
				for (int i = 0; i < message.sliceText.length; ++i) {
					newDiagnostics.append(" ");
					newDiagnostics.append(i + 1);
					newDiagnostics.append(".) ");
					newDiagnostics.append(message.sliceText[i]);
				}
				issue.setDiagnostics(newDiagnostics.toString());
			}

			oo.addIssue(issue);
		}

		// Add an information message about success, if needed
		if (messages.stream().noneMatch(m -> m.getLevel() == ValidationMessage.IssueSeverity.FATAL || m.getLevel() == ValidationMessage.IssueSeverity.ERROR)) {
			final var issue = oo.addIssue();
			issue.setSeverity(OperationOutcome.IssueSeverity.INFORMATION);
			issue.setCode(OperationOutcome.IssueType.INFORMATIONAL);
			issue.setDiagnostics("No fatal or error issues detected, the validation has passed");
		}

		return switch (this.myContext.getVersion().getVersion()) {
			case R4 -> VersionConvertorFactory_40_50.convertResource(oo);
			case R5 -> oo;
			default -> throw new MatchboxUnsupportedFhirVersionException("ValidationProvider",
																							 this.myContext.getVersion().getVersion());
		};
	}

	private IBaseResource getOoForError(final @NonNull String message) {
		final var oo = new OperationOutcome();
		final var issue = oo.addIssue();
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setCode(OperationOutcome.IssueType.EXCEPTION);
		issue.setDiagnostics(message);
		issue.addExtension().setUrl(ToolingExtensions.EXT_ISSUE_SOURCE).setValue(new StringType("ValidationProvider"));
		return VersionConvertorFactory_40_50.convertResource(oo);
	}

	public static List<ValidationMessage> doValidate(final MatchboxEngine engine,
									 String content,
									 final EncodingEnum encoding,
									 final String profile) throws EOperationOutcome, IOException {
		final List<ValidationMessage> messages = new ArrayList<>();

		if (content.startsWith("\uFEFF")) {
			content = content.replace("\uFEFF", "");
			final var m = new ValidationMessage();
			m.setLevel(ValidationMessage.IssueSeverity.WARNING);
			m.setMessage(
				"Resource content has a UTF-8 BOM marking, skipping BOM, see https://en.wikipedia.org/wiki/Byte_order_mark");
			m.setCol(0);
			m.setLine(0);
			messages.add(m);
		}

		final var format = encoding == EncodingEnum.XML ? FhirFormat.XML : FhirFormat.JSON;
		final var stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		messages.addAll(engine.validate(format, stream, profile));
		return messages;
	}
}
