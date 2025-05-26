package ch.ahdis.matchbox.validation;

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
import ca.uhn.fhir.jpa.dao.data.INpmPackageVersionResourceDao;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.util.StopWatch;
import ch.ahdis.matchbox.CliContext;
import ch.ahdis.matchbox.util.MatchboxEngineSupport;
import ch.ahdis.matchbox.validation.matchspark.LLMConnector;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.cli.VersionUtil;
import ch.ahdis.matchbox.engine.exception.MatchboxUnsupportedFhirVersionException;
import ch.ahdis.matchbox.packages.MatchboxImplementationGuideProvider;
import ch.ahdis.matchbox.registry.SimplifierPackage;
import ch.ahdis.matchbox.registry.SimplifierPackageVersionsObject;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_43_50;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r5.model.CodeableConcept;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ch.ahdis.matchbox.util.MatchboxServerUtils.addExtension;

/**
 * The HAPI provider of the operation $validate
 */
public class ValidationProvider {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationProvider.class);

	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;

	@Autowired
	protected CliContext cliContext;

	@Autowired
	private FhirContext myContext;

	@Autowired
	private MatchboxImplementationGuideProvider igProvider;
	@Autowired
	private INpmPackageVersionResourceDao myPackageVersionResourceDao;

	@Autowired
	private PlatformTransactionManager myTxManager;

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
					if (field.getType() == boolean.class || field.getType() == Boolean.class) {
						BeanUtils.setProperty(cliContext, cliContextProperty, Boolean.parseBoolean(value));
					} else {
						BeanUtils.setProperty(cliContext, cliContextProperty, value);
					}
				} catch (final IllegalAccessException | InvocationTargetException e) {
					log.error("error setting property %s to %s".formatted(cliContextProperty,
																							theRequest.getParameter(cliContextProperty)));
				}
			}
		}

		// Check if the IG should be auto-installed
		if (!cliContext.isHttpReadOnly()) {
			this.ensureIgIsInstalled(theRequest.getParameter("ig"), theRequest.getParameter("profile"));
		}

		if (theRequest.getParameter("extensions") != null) {
			String extensions = theRequest.getParameter("extensions");
			cliContext.setExtensions(new ArrayList<>(Arrays.asList(extensions.split(","))));
		}

		if (theRequest.getParameter("profile") == null) {
			return this.getOoForError("The 'profile' parameter must be provided");
		}
		String profile = theRequest.getParameter("profile");

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
		if (engine == null) {
			return this.getOoForError(
				"Matchbox engine for profile '%s' could not be created, check the installed IGs".formatted(
					profile));
		}
		int versionSeparator = profile.lastIndexOf('|');
		if (versionSeparator != -1) {
			profile = profile.substring(0, versionSeparator);
		}
		if (engine.getStructureDefinitionR5(profile) == null) {
			return this.getOoForError(
				"Engine configured, but validation for profile '%s' not found. %s".formatted(profile, engine));
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

		var oo = this.getOperationOutcome(sha3Hex, messages, profile, engine, millis, cliContext);

		Boolean aiAnalyze = null;
		// check if the request ai analyze parameter is set to true or false
		if (theRequest.getParameter("analyzeOutcomeWithAI") != null) {
			aiAnalyze = Boolean.parseBoolean(theRequest.getParameter("analyzeOutcomeWithAI"));
		}

		Boolean aiAnalyzeOnError = cliContext.getAnalyzeOutcomeWithAIOnError();

		boolean hasError = false;
		if (aiAnalyzeOnError != null && aiAnalyzeOnError) {
			for (final ValidationMessage message : messages) {
				if (message.getLevel() == ValidationMessage.IssueSeverity.ERROR || message.getLevel() == ValidationMessage.IssueSeverity.FATAL) {
					hasError = true;
					break;
				}
			}
		}
		if ((aiAnalyze != null && aiAnalyze) || (aiAnalyze == null && aiAnalyzeOnError != null && aiAnalyzeOnError && hasError)) {
			try {
				LLMConnector openAIConnector = LLMConnector.getConnector(cliContext);
				String json = FhirContext.forR5().newJsonParser().setPrettyPrint(true).encodeResourceToString(oo);
				String aiResult = openAIConnector.interpretWithMatchbox(contentString, json);
				oo = this.addAIIssueToOperationOutcome(oo, aiResult);
			} catch (Exception e) {
				log.error("Error during AI analysis", e);
				// add the error to the OperationOutcome, so the client still gets the validation result
				oo = this.addExceptionToOperationOutcome(oo, e);
			}
		}

		
		return switch (this.myContext.getVersion().getVersion()) {
			case R4 -> VersionConvertorFactory_40_50.convertResource((OperationOutcome) oo);
			case R4B -> VersionConvertorFactory_43_50.convertResource((OperationOutcome) oo);
			case R5 -> oo;
			default -> throw new MatchboxUnsupportedFhirVersionException("ValidationProvider",
																							 this.myContext.getVersion().getVersion());
		};
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

			final org.hl7.fhir.r5.model.StructureDefinition structDefR5 = engine.getStructureDefinitionR5(profile);

			final var profileDate = (structDefR5.getDateElement() != null)
				? " (%s)".formatted(structDefR5.getDateElement().asStringValue())
				: " ";

			issue.setDiagnostics(
				"Validation for profile %s|%s%s. Loaded packages: %s. Duration: %s. %s. Validation parameters: %s".formatted(
					structDefR5.getUrl(),
					structDefR5.getVersion(),
					profileDate,
					String.join(", ", engine.getContext().getLoadedPackages()),
					ms/1000.0+ "s",
					VersionUtil.getPoweredBy(),
					cliContext.toString()
				));

			var ext = issue.addExtension().setUrl("http://matchbox.health/validation");
			addExtension(ext, "profile", new UriType(structDefR5.getUrl()));
			addExtension(ext, "profileVersion", new UriType(structDefR5.getVersion()));
			addExtension(ext, "profileDate", structDefR5.getDateElement());

			ext.addExtension("total", new Duration().setUnit("ms").setValue(ms));
			addExtension(ext, "validatorVersion", new StringType(VersionUtil.getPoweredBy()));
			cliContext.addContextToExtension(ext);
			if (matchboxEngineSupport.getSessionId(engine) != null) {
				addExtension(ext, "sessionId", new StringType(matchboxEngineSupport.getSessionId(engine)));
			}
			for (final String pkg : engine.getContext().getLoadedPackages()) {
				addExtension(ext, "package", new StringType(pkg));
			}
		}

		// Map the SingleValidationMessages to OperationOutcomeIssue
		for (final ValidationMessage message : messages) {
			if (message.getType() == null) {
				// Note: this did not happen with previous core versions
				message.setType(ValidationMessage.IssueType.UNKNOWN);
			}
			final var issue = OperationOutcomeUtilities.convertToIssue(message, oo);

			// Note: the message is mapped to details.text by HAPI, but we still need it in diagnostics for the EVSClient,
			//       so we move it. This could be changed in the future.
			issue.setDiagnostics(message.getMessage());
			issue.setDetails(null);

			// Add slice info to diagnostics
			if (message.hasSliceInfo() && message.sliceHtml != null) {
				List<String> sliceInfo = engine.filterSlicingMessages(message.sliceHtml);
				if (!sliceInfo.isEmpty()) {
					final var newDiagnostics = new StringBuilder();
					newDiagnostics.append(issue.getDiagnostics());
					newDiagnostics.append(" Slice info:");

					for (int i = 0; i < sliceInfo.size(); ++i) {
						newDiagnostics.append(" ");
						newDiagnostics.append(i + 1);
						newDiagnostics.append(".) ");
						newDiagnostics.append(sliceInfo.get(i));
					}
					issue.setDiagnostics(newDiagnostics.toString());
				}
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

		return oo;
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

	private void ensureIgIsInstalled(@Nullable final String ig,
												final String profile) {
		if (ig != null) {
			// Easy case, the IG is known
			final var parts = ig.split("#");
			if (!this.igProvider.has(parts[0], parts[1])) {
				log.debug("Auto-installing the IG '{}' version '{}'", parts[0], parts[1]);
				this.igProvider.installFromInternetRegistry(parts[0], parts[1]);
			}
			return;
		}

		if (profile.startsWith("http://hl7.org/fhir/")) {
			log.debug("Profile '{}' is a core FHIR profile, no need to install an IG", profile);
			return;
		}

		// Harder case, only the profile is known
		final var parts = profile.split("\\|");
		final String canonical;
		final String version;
		if (parts.length == 2) {
			final Boolean found = new TransactionTemplate(this.myTxManager)
				.execute(tx -> this.myPackageVersionResourceDao.getPackageVersionByCanonicalAndVersion(parts[0], parts[1]).isPresent());
			if (found != null && found) {
				// The profile was found in the database, the IG is installed
				return;
			}
			canonical = parts[0];
			version = parts[1];
		} else {
			canonical = profile;
			version = null;
		}

		// The profile was not found in the database, we need to search for the IG that contains it
		final var gson = new Gson();
		final var igSearchUrl =
			"https://packages.simplifier.net/catalog?canonical=%s&prerelease=false".formatted(URLEncoder.encode(canonical, StandardCharsets.UTF_8));
		final SimplifierPackage[] packages;
		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
			final var httpget = new HttpGet(igSearchUrl);
			packages = httpclient.execute(httpget, response ->
					 gson.fromJson(new InputStreamReader(response.getEntity().getContent()), SimplifierPackage[].class));
		} catch (final Exception e) {
			log.error("Error while searching for IGs", e);
			return;
		}

		if (packages == null || packages.length == 0) {
			return;
		}
		log.debug("Found {} IGs for profile '{}', checking the first one ('{}')", packages.length, profile,
					 packages[0].getName());
		if (version != null) {
			// This might not be always true, but it's a good heuristic: let's use the profile version as the IG version
			this.igProvider.installFromInternetRegistry(packages[0].getName(), version);
			return;
		}
		// We have to search for the latest profile version
		final var versionSearchUrl = "https://packages.simplifier.net/" + packages[0].getName();
		final SimplifierPackageVersionsObject versionsObject;
		try (final CloseableHttpClient httpclient = HttpClients.createDefault()) {
			final var httpget = new HttpGet(versionSearchUrl);
			versionsObject = httpclient.execute(httpget, response ->
				gson.fromJson(new InputStreamReader(response.getEntity().getContent()), SimplifierPackageVersionsObject.class));
		} catch (final Exception e) {
			log.error("Error while searching for IG versions", e);
			return;
		}

		if (versionsObject == null || versionsObject.getVersions() == null || versionsObject.getVersions().isEmpty()) {
			return;
		}

		final var latestVersion = versionsObject.getVersions().keySet().toArray()[ versionsObject.getVersions().size()-1].toString();
		try {
			this.igProvider.installFromInternetRegistry(packages[0].getName(), latestVersion);
		} catch (final Exception e) {
			log.error("Error while installing IG version", e);
		}
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
		try {
			messages.addAll(engine.validate(format, stream, profile));
		} catch (IOException e) {
			log.error("Internal validation error", e);
			final var m = new ValidationMessage();
			m.setLevel(ValidationMessage.IssueSeverity.FATAL);
			m.setMessage(
				"Internal validation exception, contact support "+e.getMessage());
			m.setCol(0);
			m.setLine(0);
			messages.add(m);
		}
		return messages;
	}

	public IBaseResource addAIIssueToOperationOutcome(IBaseResource resource, String aiResponse) {
		if (resource instanceof final OperationOutcome outcome) {
			final var details = new CodeableConcept();
			details.setText("AI Analyze of the Operation Outcome");

			outcome.addIssue()
				.setSeverity(OperationOutcome.IssueSeverity.INFORMATION)
				.setCode(OperationOutcome.IssueType.INFORMATIONAL)
				.setDiagnostics(aiResponse)
				.setDetails(details)
				.addExtension()
					.setUrl("http://hl7.org/fhir/StructureDefinition/rendering-style")
					.setValue(new StringType("markdown"));

			return outcome;
		}
		throw new IllegalArgumentException("Provided resource is not an OperationOutcome.");
	}

	public IBaseResource addExceptionToOperationOutcome(IBaseResource resource, Exception e) {
		if (resource instanceof final OperationOutcome outcome) {
			outcome.addIssue()
				.setSeverity(OperationOutcome.IssueSeverity.ERROR)
				.setCode(OperationOutcome.IssueType.EXCEPTION)
				.setDiagnostics(e.getLocalizedMessage());
			return outcome;
		}
		throw new IllegalArgumentException("Provided resource is not an OperationOutcome.");
	}
}
