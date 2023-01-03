package ch.ahdis.fhir.hapi.jpa.validation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseParameters;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.StructureDefinition;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.utils.EOperationOutcome;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.DataFormatException;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.util.ParametersUtil;
import ca.uhn.fhir.util.StopWatch;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationOptions;
import ca.uhn.fhir.validation.ValidationResult;
import ch.ahdis.matchbox.MatchboxEngineSupport;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.cli.VersionUtil;

/**
 * Operation $validate
 */
public class ValidationProvider {

	@Autowired
	protected MatchboxEngineSupport matchboxEngineSupport;

	// @Autowired
	// protected DefaultProfileValidationSupport defaultProfileValidationSuport;
	/*
	 * @Autowired
	 * 
	 * @Qualifier("myJpaValidationSupport") protected IValidationSupport
	 * myJpaValidationSupport;
	 */
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationProvider.class);

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
			@OperationParam(name = "return", type = IBase.class, min = 1, max = 1) })
	public IBaseResource validate(HttpServletRequest theRequest) {

		FhirContext myFhirCtx = FhirContext.forR4Cached();

		log.info("$validate");
		ArrayList<SingleValidationMessage> addedValidationMessages = new ArrayList<>();

		StopWatch sw = new StopWatch();
		sw.startTask("Total");

		String profile = null;
		boolean reload = false;

		ValidationOptions validationOptions = new ValidationOptions();
		if (theRequest.getParameter("profile") != null) {
			profile = theRequest.getParameter("profile");
		}

		if (theRequest.getParameter("reload") != null) {
			reload = true;
		}

		MatchboxEngine engine = matchboxEngineSupport.getMatchboxEngine(profile, reload);
		if (engine == null) {
			return getValidationMessageProfileNotSupported(profile);
		}
		if (engine.getStructureDefinition(profile) == null) {
			return getValidationMessageProfileNotSupported(profile);
		}
		
		if (!matchboxEngineSupport.isInitialized()) {
			OperationOutcomeIssueComponent a = new OperationOutcomeIssueComponent().setDiagnostics("validaiton engine not initialized, please try again").setSeverity(IssueSeverity.ERROR);
			return new OperationOutcome().addIssue(a);
		}

		validationOptions.addProfileIfNotBlank(profile);
		StructureDefinition structDef = engine.getStructureDefinition(profile);
		String contentString = getContentString(theRequest, addedValidationMessages);

		if (contentString.length() == 0) {
			SingleValidationMessage m = new SingleValidationMessage();
			m.setSeverity(ResultSeverityEnum.ERROR);
			m.setMessage("No resource provided in http body");
			m.setLocationCol(0);
			m.setLocationLine(0);
			addedValidationMessages.add(m);
			return new ValidationResultWithExtensions(myFhirCtx, addedValidationMessages).toOperationOutcome();
		} else {
			log.info(contentString);
		}

		String sha3Hex = new DigestUtils("SHA3-256").digestAsHex(contentString + (profile != null ? profile : ""));

		EncodingEnum encoding = EncodingEnum.forContentType(theRequest.getContentType());
		if (encoding == null) {
			encoding = EncodingEnum.detectEncoding(contentString);
		}

		// the $validate operation can be called in different ways, see
		// https://www.hl7.org/fhir/resource-operation-validate.html
		// HTTP Body ---- HTTP Header Parameter
		// Resource profile = specified --> return OperationOutcome
		// Parameters profile = specified --> return OperationOutcome
		// Resource profile not specified --> return OperationOutcome
		// Parameters profile not specified --> return Parameters/OperationOutcome
		ValidationResult result = null;
		IBaseResource resource = null;
		try {
			// we still parse to catch wrongli formatted (doest not work for CDA) thats why we don't throw an exception now
 			resource = encoding.newParser(myFhirCtx).parseResource(contentString);
		} catch (DataFormatException e) {
//			return getValidationMessageDataFormatException(e);
		}

		if (resource != null && "Parameters".equals(resource.fhirType()) && profile == null) {
//      IBaseParameters parameters = (IBaseParameters) resource;
// https://github.com/ahdis/matchbox/issues/11
			Parameters parameters = (Parameters) resource;
			IBaseResource resourceInParam = null;
			for (ParametersParameterComponent compoment : parameters.getParameter()) {
				if ("resource".equals(compoment.getName())) {
					resourceInParam = compoment.getResource();
					break;
				}
			}
			if (resourceInParam != null && resourceInParam.fhirType().contentEquals("Bundle")) {
				Bundle bundle = (Bundle) resourceInParam;
				for (BundleEntryComponent entry : bundle.getEntry()) {
					if (entry.getResource() != null && entry.getResource().getId() == null) {
						if (entry.getFullUrl() != null && entry.getFullUrl().startsWith("urn:uuid:")) {
							entry.setId(entry.getFullUrl().substring(9));
						}
					}

				}
			}
			List<String> profiles = ParametersUtil.getNamedParameterValuesAsString(myFhirCtx, parameters, "profile");
			if (profiles != null && profiles.size() == 1) {
				profile = profiles.get(0);
			}
			if (resourceInParam != null) {
				validationOptions = new ValidationOptions();
				if (profile != null) {
					structDef = (StructureDefinition) engine.getStructureDefinition(profile);
					if (structDef == null) {
						return getValidationMessageProfileNotSupported(profile);
					}
					validationOptions.addProfileIfNotBlank(profile);
				}
				result = validateWithResult(engine, encoding.newParser(myFhirCtx).encodeResourceToString(resourceInParam), validationOptions, encoding, profile);
				IBaseResource operationOutcome = getOperationOutcome(sha3Hex, addedValidationMessages, sw, structDef, result, engine);
				IBaseParameters returnParameters = ParametersUtil.newInstance(myFhirCtx);
				ParametersUtil.addParameterToParameters(myFhirCtx, returnParameters, "return", operationOutcome);
				return returnParameters;
			} else {
				// we have a validation for a Parameter but not a resource inside, fall back to
				// validate only Parameter
				result = validateWithResult(engine, contentString, validationOptions, encoding, profile);
			}
		} else {
			result = validateWithResult(engine, contentString, validationOptions, encoding, profile);
		}
		return getOperationOutcome(sha3Hex, addedValidationMessages, sw, structDef, result, engine);
	}

	private ValidationResult validateWithResult(MatchboxEngine engine, String contentString,
			ValidationOptions validationOptions, EncodingEnum encoding, String profile) {
		try {
			ArrayList<SingleValidationMessage> hapiMessages = new ArrayList<SingleValidationMessage>();
			List<ValidationMessage> messages = engine.validate(
					(encoding == EncodingEnum.XML ? FhirFormat.XML : FhirFormat.JSON),
					new ByteArrayInputStream(contentString.getBytes("UTF-8")), profile);

			for (ValidationMessage riMessage : messages) {
				SingleValidationMessage hapiMessage = new SingleValidationMessage();
				if (riMessage.getCol() != -1) {
					hapiMessage.setLocationCol(riMessage.getCol());
				}
				if (riMessage.getLine() != -1) {
					hapiMessage.setLocationLine(riMessage.getLine());
				}
				hapiMessage.setLocationString(riMessage.getLocation());
				hapiMessage.setMessage(riMessage.getMessage());

				// MATCHBOX added
				String message = riMessage.getMessage();
				ValidationMessage vm = riMessage;
				if (vm != null && vm.sliceText != null) {
					message += " Slice info:";
					for (int i = 0; i < vm.sliceText.length; ++i) {
						String s = vm.sliceText[i];
						message += " " + (i + 1) + ".) " + s;
					}
				}
				hapiMessage.setMessage(message);
				if (riMessage.getLevel() != null) {
					hapiMessage.setSeverity(ResultSeverityEnum.fromCode(riMessage.getLevel().toCode()));
				}
				if (riMessage.getMessageId() != null) {
					hapiMessage.setMessageId(riMessage.getMessageId());
				}
				// theCtx.addValidationMessage(hapiMessage);
				hapiMessages.add(hapiMessage);
			}
			ValidationResult result = new ValidationResult(FhirContext.forR4Cached(), hapiMessages);
			return result;
		} catch (FHIRException e) {
		} catch (UnsupportedEncodingException e) {
		} catch (IOException e) {
		} catch (EOperationOutcome e) {
		}
		return null;
	}

//

	private String getContentString(HttpServletRequest theRequest,
			ArrayList<SingleValidationMessage> addedValidationMessages) {
		byte[] bytes = null;
		String contentString = "";
		try {
			bytes = IOUtils.toByteArray(theRequest.getInputStream());
			if (bytes.length > 2 && bytes[0] == -17 && bytes[1] == -69 && bytes[2] == -65) {
				byte[] dest = new byte[bytes.length - 3];
				System.arraycopy(bytes, 3, dest, 0, bytes.length - 3);
				bytes = dest;
				if (addedValidationMessages != null) {
					SingleValidationMessage m = new SingleValidationMessage();
					m.setSeverity(ResultSeverityEnum.WARNING);
					m.setMessage(
							"Resource content has a UTF-8 BOM marking, skipping BOM, see https://en.wikipedia.org/wiki/Byte_order_mark");
					m.setLocationCol(0);
					m.setLocationLine(0);
					addedValidationMessages.add(m);
				}
			}
			contentString = new String(bytes);
		} catch (IOException e) {
		}
		return contentString;
	}

	private IBaseResource getOperationOutcome(String id, ArrayList<SingleValidationMessage> addedValidationMessages,
			StopWatch sw, StructureDefinition profile, ValidationResult result, MatchboxEngine engine) {
		sw.endCurrentTask();

		FhirContext myFhirCtx = FhirContext.forR4Cached();

		log.info("Validation time: " + sw.toString());
		
		String packages = "with packages: ";
		List<String> pkgs = engine.getContext().getLoadedPackages();
		for(int i=0; i<pkgs.size(); ++i) {
			if (i>0) {
				packages += ", ";
			}
			packages += pkgs.get(i);
		}

		SingleValidationMessage m = new SingleValidationMessage();
		m.setSeverity(ResultSeverityEnum.INFORMATION);
		m.setMessage("Validation "
				+ (profile != null
						? "for profile " + profile.getUrl() + "|" + profile.getVersion() + " "
								+ (profile.getDateElement() != null ? "(" + profile.getDateElement().asStringValue() + ") " : " ")
						: "") +packages + " " 
				+ (result.getMessages().size() == 0 ? "No Issues detected. " : "") + sw.formatTaskDurations() + " "
				+ VersionUtil.getPoweredBy());
	 	
		m.setLocationCol(0);
		m.setLocationLine(0);
		addedValidationMessages.add(m);

		addedValidationMessages.addAll(result.getMessages());

		IBaseResource operationOutcome = new ValidationResultWithExtensions(myFhirCtx, addedValidationMessages)
				.toOperationOutcome();
		operationOutcome.setId(id);

		log.info(myFhirCtx.newXmlParser().encodeResourceToString(operationOutcome));

		return operationOutcome;
	}

	private IBaseResource getValidationMessageProfileNotSupported(String profile) {
		SingleValidationMessage m = new SingleValidationMessage();
		m.setSeverity(ResultSeverityEnum.ERROR);
		m.setMessage("Validation for profile " + profile
				+ " not supported by this server, but additional ig's could be configured.");
		m.setLocationCol(0);
		m.setLocationLine(0);
		ArrayList<SingleValidationMessage> newValidationMessages = new ArrayList<>();
		newValidationMessages.add(m);
		return (new ValidationResult(FhirContext.forR4Cached(), newValidationMessages)).toOperationOutcome();
	}

	private IBaseResource getValidationMessageDataFormatException(DataFormatException e) {
		SingleValidationMessage m = new SingleValidationMessage();
		m.setSeverity(ResultSeverityEnum.FATAL);
		m.setMessage(e.getMessage());
		m.setLocationCol(0);
		m.setLocationLine(0);
		ArrayList<SingleValidationMessage> newValidationMessages = new ArrayList<>();
		newValidationMessages.add(m);
		return (new ValidationResult(FhirContext.forR4Cached(), newValidationMessages)).toOperationOutcome();
	}

}
