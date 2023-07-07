package org.hl7.fhir.r5.terminologies;

/*
  Copyright (c) 2011+, HL7, Inc.
  All rights reserved.

  Redistribution and use in source and binary forms, with or without modification,
  are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
     list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
     this list of conditions and the following disclaimer in the documentation
     and/or other materials provided with the distribution.
 * Neither the name of HL7 nor the names of its contributors may be used to
     endorse or promote products derived from this software without specific
     prior written permission.

  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.

 */



import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.NoTerminologyServiceException;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.context.IWorkerContext.ValidationResult;
import org.hl7.fhir.r5.model.CanonicalType;
import org.hl7.fhir.r5.model.CodeSystem;
import org.hl7.fhir.r5.model.Enumerations.CodeSystemContentMode;
import org.hl7.fhir.r5.model.Enumerations.FilterOperator;
import org.hl7.fhir.r5.model.CodeSystem.ConceptDefinitionComponent;
import org.hl7.fhir.r5.model.CodeSystem.ConceptDefinitionDesignationComponent;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.OperationOutcome.IssueType;
import org.hl7.fhir.r5.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r5.model.PackageInformation;
import org.hl7.fhir.r5.model.UriType;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.ValueSet.ConceptReferenceComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptReferenceDesignationComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetComponent;
import org.hl7.fhir.r5.model.ValueSet.ConceptSetFilterComponent;
import org.hl7.fhir.r5.model.ValueSet.ValueSetExpansionContainsComponent;
import org.hl7.fhir.r5.terminologies.ValueSetCheckerSimple.ConceptReferencePair;
import org.hl7.fhir.r5.terminologies.ValueSetExpander.TerminologyServiceErrorClass;
import org.hl7.fhir.r5.terminologies.ValueSetExpander.ValueSetExpansionOutcome;
import org.hl7.fhir.r5.utils.ToolingExtensions;
import org.hl7.fhir.r5.utils.validation.ValidationContextCarrier;
import org.hl7.fhir.r5.utils.validation.ValidationContextCarrier.ValidationContextResourceProxy;
import org.hl7.fhir.utilities.CommaSeparatedStringBuilder;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationOptions;
import org.hl7.fhir.utilities.validation.ValidationOptions.ValueSetMode;

import com.google.j2objc.annotations.ReflectionSupport.Level;

public class ValueSetCheckerSimple extends ValueSetWorker implements ValueSetChecker {

	public static class VSCheckerException extends FHIRException {

		private List<OperationOutcomeIssueComponent> issues;
		private boolean warning; // MATCHBOX Backported from 7166d55

		public VSCheckerException(String message, List<OperationOutcomeIssueComponent> issues) {
			super(message);
			this.issues = issues;
		}

		// MATCHBOX Backported from 7166d55
		public VSCheckerException(String message, List<OperationOutcomeIssueComponent> issues, boolean warning) {
			super(message);
			this.issues = issues;
			this.warning = warning;
		}

		public List<OperationOutcomeIssueComponent> getIssues() {
			return issues;
		}

		private static final long serialVersionUID = -5889505119633054187L;

		// MATCHBOX Backported from 7166d55
		public boolean isWarning() {
			return warning;
		}
	}
	public class ConceptReferencePair {

		private ValueSet valueset;
		private ConceptReferenceComponent cc;

		public ConceptReferencePair(ValueSet valueset, ConceptReferenceComponent cc) {
			this.valueset = valueset;
			this.cc = cc;
		}

		public ValueSet getValueset() {
			return valueset;
		}

		public ConceptReferenceComponent getCc() {
			return cc;
		}

	}


	private ValueSet valueset;
	private IWorkerContext context;
	private Map<String, ValueSetCheckerSimple> inner = new HashMap<>();
	private ValidationOptions options;
	private ValidationContextCarrier localContext;
	private List<CodeSystem> localSystems = new ArrayList<>();

	public ValueSetCheckerSimple(ValidationOptions options, ValueSet source, IWorkerContext context) {
		this.valueset = source;
		this.context = context;
		this.options = options;
	}

	public ValueSetCheckerSimple(ValidationOptions options, ValueSet source, IWorkerContext context, ValidationContextCarrier ctxt) {
		this.valueset = source;
		this.context = context;
		this.options = options.copy();
		this.options.setEnglishOk(true);
		this.localContext = ctxt;
		analyseValueSet();
	}

	private void analyseValueSet() {
		if (localContext != null) {
			if (valueset != null) {
				for (ConceptSetComponent i : valueset.getCompose().getInclude()) {
					analyseComponent(i);
				}
				for (ConceptSetComponent i : valueset.getCompose().getExclude()) {
					analyseComponent(i);
				}
			}
		}
	}

	private void analyseComponent(ConceptSetComponent i) {
		if (i.getSystemElement().hasExtension(ToolingExtensions.EXT_VALUESET_SYSTEM)) {
			String ref = i.getSystemElement().getExtensionString(ToolingExtensions.EXT_VALUESET_SYSTEM);
			if (ref.startsWith("#")) {
				String id = ref.substring(1);
				for (ValidationContextResourceProxy t : localContext.getResources()) {
					CodeSystem cs = (CodeSystem) t.loadContainedResource(id, CodeSystem.class);
					if (cs != null) {
						localSystems.add(cs);
					}
				}
			} else {
				throw new Error("Not done yet #2: "+ref);
			}
		}
	}

	public ValidationResult validateCode(CodeableConcept code) throws FHIRException {
		return validateCode("CodeableConcept", code);
	}

	public ValidationResult validateCode(String path, CodeableConcept code) throws FHIRException {
		// first, we validate the codings themselves
		ValidationProcessInfo info = new ValidationProcessInfo();
		if (options.getValueSetMode() != ValueSetMode.CHECK_MEMERSHIP_ONLY) {
			int i = 0;
			for (Coding c : code.getCoding()) {
				if (!c.hasSystem()) {
					info.addIssue(makeIssue(IssueSeverity.WARNING, IssueType.UNKNOWN, path, context.formatMessage(I18nConstants.CODING_HAS_NO_SYSTEM__CANNOT_VALIDATE)));
				}
				CodeSystem cs = resolveCodeSystem(c.getSystem(), c.getVersion());
				ValidationResult res = null;
				if (cs == null || cs.getContent() != CodeSystemContentMode.COMPLETE) {
					if (context.isNoTerminologyServer()) {
						if (c.hasVersion()) {
							String msg = context.formatMessage(I18nConstants.UNKNOWN_CODESYSTEM_VERSION, c.getSystem(), c.getVersion() , resolveCodeSystemVersions(c.getSystem()).toString());
							res = new ValidationResult(IssueSeverity.ERROR, msg, makeIssue(IssueSeverity.ERROR, IssueType.NOTFOUND, path+".coding["+i+"].system", msg));
						} else {
							String msg = context.formatMessage(I18nConstants.UNKNOWN_CODESYSTEM, c.getSystem(), c.getVersion());
							res = new ValidationResult(IssueSeverity.ERROR, msg, makeIssue(IssueSeverity.ERROR, IssueType.NOTFOUND, path+".coding["+i+"].system", msg));
						}
					} else {
						res = context.validateCode(options.withNoClient(), c, null);
					}
				} else {
					res = validateCode(path+".coding["+i+"]", c, cs);
				}
				info.getIssues().addAll(res.getIssues());
				i++;
			}
		}
		Coding foundCoding = null;
		if (valueset != null && options.getValueSetMode() != ValueSetMode.NO_MEMBERSHIP_CHECK) {
			Boolean result = false;
			CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder(", ");

			for (Coding c : code.getCoding()) {
				b.append(c.getSystem()+(c.hasVersion() ? "|"+c.getVersion() : "")+"#"+c.getCode());
				Boolean ok = codeInValueSet(c.getSystem(), c.getVersion(), c.getCode(), info);
				if (ok == null && result == false) {
					result = null;
				} else if (ok) {
					result = true;
					foundCoding = c;
				}
			}
			if (result == null) {
				String msg = context.formatMessage(I18nConstants.UNABLE_TO_CHECK_IF_THE_PROVIDED_CODES_ARE_IN_THE_VALUE_SET_, valueset.getUrl(), b.toString());
				info.getIssues().addAll(makeIssue(IssueSeverity.WARNING, IssueType.INVALID, path, msg));
			} else if (!result) {
				String msg = context.formatMessagePlural(code.getCoding().size(), I18nConstants.NONE_OF_THE_PROVIDED_CODES_ARE_IN_THE_VALUE_SET_, valueset.getUrl(), b.toString());
				info.getIssues().addAll(makeIssue(IssueSeverity.ERROR, IssueType.INVALID, path, msg));
			}
		}
		if (info.hasErrors()) {
			ValidationResult res = new ValidationResult(IssueSeverity.ERROR, info.summary(), info.getIssues());
			if (foundCoding != null) {
				ConceptDefinitionComponent cd = new ConceptDefinitionComponent(foundCoding.getCode());
				cd.setDisplay(lookupDisplay(foundCoding));
				res.setDefinition(cd);
				res.setSystem(foundCoding.getSystem());
				res.setDisplay(cd.getDisplay());
			}
			return res;
		} else if (foundCoding == null) {
			return new ValidationResult(IssueSeverity.ERROR, "Internal Error that should not happen", makeIssue(IssueSeverity.FATAL, IssueType.EXCEPTION, path, "Internal Error that should not happen"));
		} else if (info.getIssues().size() > 0) {
			String disp = lookupDisplay(foundCoding);
			ConceptDefinitionComponent cd = new ConceptDefinitionComponent(foundCoding.getCode());
			cd.setDisplay(disp);
			return new ValidationResult(IssueSeverity.WARNING, info.summary(), foundCoding.getSystem(), cd, disp, info.getIssues());
		} else {
			ConceptDefinitionComponent cd = new ConceptDefinitionComponent(foundCoding.getCode());
			cd.setDisplay(lookupDisplay(foundCoding));
			return new ValidationResult(foundCoding.getSystem(), cd, getPreferredDisplay(cd, null));
		}
	}

	private String lookupDisplay(Coding c) {
		CodeSystem cs = resolveCodeSystem(c.getSystem(), c.getVersion());
		if (cs != null) {
			ConceptDefinitionComponent cd = CodeSystemUtilities.getCode(cs, c.getCode());
			if (cd != null) {
				return getPreferredDisplay(cd, cs);
			}
		}
		return null;
	}

	public CodeSystem resolveCodeSystem(String system, String version) {
		for (CodeSystem t : localSystems) {
			if (t.getUrl().equals(system) && versionsMatch(version, t.getVersion())) {
				return t;
			}
		}
		CodeSystem cs = context.fetchCodeSystem(system, version);
		if (cs == null) {
			cs = findSpecialCodeSystem(system, version);
		}
		return cs;
	}

	public List<String> resolveCodeSystemVersions(String system) {
		List<String> res = new ArrayList<>();
		for (CodeSystem t : localSystems) {
			if (t.getUrl().equals(system) && t.hasVersion()) {
				res.add(t.getVersion());
			}
		}
		res.addAll(new ContextUtilities(context).fetchCodeSystemVersions(system));
		return res;
	}

	private boolean versionsMatch(String versionTest, String versionActual) {
		return versionTest == null && VersionUtilities.versionsMatch(versionTest, versionActual);
	}

	private List<OperationOutcomeIssueComponent> makeIssue(IssueSeverity level, IssueType type, String location, String message) {
		OperationOutcomeIssueComponent result = new OperationOutcomeIssueComponent();
		switch (level) {
			case ERROR:
				result.setSeverity(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.ERROR);
				break;
			case FATAL:
				result.setSeverity(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.FATAL);
				break;
			case INFORMATION:
				result.setSeverity(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.INFORMATION);
				break;
			case WARNING:
				result.setSeverity(org.hl7.fhir.r5.model.OperationOutcome.IssueSeverity.WARNING);
				break;
		}
		result.setCode(type);
		result.addLocation(location);
		result.getDetails().setText(message);
		ArrayList<OperationOutcomeIssueComponent> list = new ArrayList<>();
		list.add(result);
		return list;
	}

	public ValidationResult validateCode(Coding code) throws FHIRException {
		return validateCode("Coding", code);
	}

	public ValidationResult validateCode(String path, Coding code) throws FHIRException {
		String warningMessage = null;
		// first, we validate the concept itself

		ValidationResult res = null;
		boolean inExpansion = false;
		boolean inInclude = false;
		String system = code.hasSystem() ? code.getSystem() : getValueSetSystemOrNull();
		if (options.getValueSetMode() != ValueSetMode.CHECK_MEMERSHIP_ONLY) {
			if (system == null && !code.hasDisplay()) { // dealing with just a plain code (enum)
				List<String> problems = new ArrayList<>();
				system = systemForCodeInValueSet(code.getCode(), problems);
				if (system == null) {
					if (problems.size() == 0) {
						throw new Error("Unable to resolve systems but no reason why"); // this is an error in the java code
					} else if (problems.size() == 1) {
						return new ValidationResult(IssueSeverity.ERROR, problems.get(0), makeIssue(IssueSeverity.ERROR, IssueType.UNKNOWN, path, problems.get(0)));
					} else {
						ValidationResult vr = new ValidationResult(IssueSeverity.ERROR, problems.toString(), null);
						for (String s : problems) {
							vr.getIssues().addAll(makeIssue(IssueSeverity.ERROR, IssueType.UNKNOWN, path, s));
						}
						return vr;
					}
				}
			}
			if (!code.hasSystem()) {
				if (options.isGuessSystem() && system == null && Utilities.isAbsoluteUrl(code.getCode())) {
					system = "urn:ietf:rfc:3986"; // this arises when using URIs bound to value sets
				}
				code.setSystem(system);
			}
			inExpansion = checkExpansion(code);
			inInclude = checkInclude(code);
			CodeSystem cs = resolveCodeSystem(system, code.getVersion());
			if (cs == null) {
				if (code.getVersion() == null) {
					warningMessage = context.formatMessage(I18nConstants.UNKNOWN_CODESYSTEM, system);
				} else {
					warningMessage = context.formatMessage(I18nConstants.UNKNOWN_CODESYSTEM_VERSION, system, code.getVersion(), resolveCodeSystemVersions(system).toString());
				}
				if (!inExpansion) {
					if (valueset != null && valueset.hasExpansion()) {
						String msg = context.formatMessage(I18nConstants.CODESYSTEM_CS_UNK_EXPANSION,
																	  valueset.getUrl(),
																	  code.getSystem(),
																	  code.getCode().toString());
						List<OperationOutcomeIssueComponent> issues = new ArrayList<>();
						issues.addAll(makeIssue(IssueSeverity.ERROR, IssueType.NOTFOUND, path, msg));
						throw new VSCheckerException(msg, issues);
					} else {
						List<OperationOutcomeIssueComponent> issues = new ArrayList<>();
						issues.addAll(makeIssue(IssueSeverity.ERROR, IssueType.NOTFOUND, path+".system", warningMessage));
						if (valueset == null) {
							throw new VSCheckerException(warningMessage, issues);
						} else {
							String msg = context.formatMessagePlural(1, I18nConstants.NONE_OF_THE_PROVIDED_CODES_ARE_IN_THE_VALUE_SET_, valueset.getUrl(), code.toString());
							issues.addAll(makeIssue(IssueSeverity.ERROR, IssueType.INVALID, path, msg));
							throw new VSCheckerException(warningMessage+"; "+msg, issues);
						}
					}
				}
			}
			if (cs != null && cs.hasSupplements()) {
				String msg = context.formatMessage(I18nConstants.CODESYSTEM_CS_NO_SUPPLEMENT, cs.getUrl());
				return new ValidationResult(IssueSeverity.ERROR, msg, makeIssue(IssueSeverity.ERROR, IssueType.NOTFOUND, path, msg));
			}
			if (cs!=null && cs.getContent() != CodeSystemContentMode.COMPLETE) {
				warningMessage = "Resolved system "+system+(cs.hasVersion() ? " (v"+cs.getVersion()+")" : "")+", but the definition is not complete";
				if (!inExpansion && cs.getContent() != CodeSystemContentMode.FRAGMENT) { // we're going to give it a go if it's a fragment
					throw new VSCheckerException(warningMessage, null);
				}
			}

			if (cs != null /*&& (cs.getContent() == CodeSystemContentMode.COMPLETE || cs.getContent() == CodeSystemContentMode.FRAGMENT)*/) {
				if (!(cs.getContent() == CodeSystemContentMode.COMPLETE || cs.getContent() == CodeSystemContentMode.FRAGMENT)) {
					if (inInclude) {
						ConceptReferenceComponent cc = findInInclude(code);
						if (cc != null) {
							// we'll take it on faith
							String disp = getPreferredDisplay(cc);
							res = new ValidationResult(system, new ConceptDefinitionComponent().setCode(cc.getCode()).setDisplay(disp), disp);
							res.setMessage("Resolved system "+system+", but the definition is not complete, so assuming value set include is correct");
							return res;
						}
					}
					// we can't validate that here.
					throw new FHIRException("Unable to evaluate based on empty code system");
				}
				res = validateCode(path, code, cs);
			} else if (cs == null && valueset.hasExpansion() && inExpansion) {
				// we just take the value set as face value then
				res = new ValidationResult(system, new ConceptDefinitionComponent().setCode(code.getCode()).setDisplay(code.getDisplay()), code.getDisplay());
			} else {
				// well, we didn't find a code system - try the expansion?
				// disabled waiting for discussion
				throw new FHIRException("No try the server");
			}
		} else {
			inExpansion = checkExpansion(code);
			inInclude = checkInclude(code);
		}

		ValidationProcessInfo info = new ValidationProcessInfo();

		// then, if we have a value set, we check it's in the value set
		if (valueset != null && options.getValueSetMode() != ValueSetMode.NO_MEMBERSHIP_CHECK) {
			if ((res==null || res.isOk())) {
				Boolean ok = codeInValueSet(system, code.getVersion(), code.getCode(), info);
				if (ok == null || !ok) {
					if (res == null) {
						res = new ValidationResult((IssueSeverity) null, null, info.getIssues());
					}
					if (info.getErr() != null) {
						res.setErrorClass(info.getErr());
					}
					if (ok == null) {
						res.setMessage("Unable to check whether code is in value set "+valueset.getUrl()+": "+info.summary()).setSeverity(IssueSeverity.WARNING);
						res.getIssues().addAll(makeIssue(IssueSeverity.WARNING, IssueType.EXCEPTION, path, res.getMessage()));
					} else if (!inExpansion && !inInclude) {
						if (!info.getIssues().isEmpty()) {
							res.setMessage("Not in value set "+valueset.getUrl()+": "+info.summary()).setSeverity(IssueSeverity.ERROR);
							res.getIssues().addAll(makeIssue(IssueSeverity.ERROR, IssueType.INVALID, path, res.getMessage()));
						} else {
							String msg = context.formatMessagePlural(1, I18nConstants.NONE_OF_THE_PROVIDED_CODES_ARE_IN_THE_VALUE_SET_, valueset.getUrl(), code.toString());
							res.setMessage(msg).setSeverity(IssueSeverity.ERROR);
							res.getIssues().addAll(makeIssue(IssueSeverity.ERROR, IssueType.INVALID, path, msg));
							res.setDefinition(null);
							res.setSystem(null);
							res.setDisplay(null);
						}
					} else if (warningMessage!=null) {
						String msg = context.formatMessage(I18nConstants.CODE_FOUND_IN_EXPANSION_HOWEVER_, warningMessage);
						res = new ValidationResult(IssueSeverity.WARNING, msg, makeIssue(IssueSeverity.WARNING, IssueType.EXCEPTION, path, msg));
					} else if (inExpansion) {
						res.setMessage("Code found in expansion, however: " + res.getMessage());
						res.getIssues().addAll(makeIssue(IssueSeverity.WARNING, IssueType.EXCEPTION, path, res.getMessage()));
					} else if (inInclude) {
						res.setMessage("Code found in include, however: " + res.getMessage());
						res.getIssues().addAll(makeIssue(IssueSeverity.WARNING, IssueType.EXCEPTION, path, res.getMessage()));
					}
				}
			} else if ((res != null && !res.isOk())) {
				String msg = context.formatMessagePlural(1, I18nConstants.NONE_OF_THE_PROVIDED_CODES_ARE_IN_THE_VALUE_SET_, valueset.getUrl(), code.toString());
				res.setMessage(res.getMessage()+"; "+msg);
				res.getIssues().addAll(makeIssue(IssueSeverity.ERROR, IssueType.INVALID, path, msg));
			}
		}
		return res;
	}

	private boolean checkInclude(Coding code) {
		if (valueset == null || code.getSystem() == null || code.getCode() == null) {
			return false;
		}
		for (ConceptSetComponent inc : valueset.getCompose().getExclude()) {
			if (inc.hasSystem() && inc.getSystem().equals(code.getSystem())) {
				for (ConceptReferenceComponent cc : inc.getConcept()) {
					if (cc.hasCode() && cc.getCode().equals(code.getCode())) {
						return false;
					}
				}
			}
		}
		for (ConceptSetComponent inc : valueset.getCompose().getInclude()) {
			if (inc.hasSystem() && inc.getSystem().equals(code.getSystem())) {
				for (ConceptReferenceComponent cc : inc.getConcept()) {
					if (cc.hasCode() && cc.getCode().equals(code.getCode())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private ConceptReferenceComponent findInInclude(Coding code) {
		if (valueset == null || code.getSystem() == null || code.getCode() == null) {
			return null;
		}
		for (ConceptSetComponent inc : valueset.getCompose().getInclude()) {
			if (inc.hasSystem() && inc.getSystem().equals(code.getSystem())) {
				for (ConceptReferenceComponent cc : inc.getConcept()) {
					if (cc.hasCode() && cc.getCode().equals(code.getCode())) {
						return cc;
					}
				}
			}
		}
		return null;
	}

	private CodeSystem findSpecialCodeSystem(String system, String version) {
		if ("urn:ietf:rfc:3986".equals(system)) {
			CodeSystem cs = new CodeSystem();
			cs.setUrl(system);
			cs.setUserData("tx.cs.special", new URICodeSystem());
			cs.setContent(CodeSystemContentMode.COMPLETE);
			return cs;
		}
		return null;
	}

	private ValidationResult findCodeInExpansion(Coding code) {
		if (valueset==null || !valueset.hasExpansion())
			return null;
		return findCodeInExpansion(code, valueset.getExpansion().getContains());
	}

	private ValidationResult findCodeInExpansion(Coding code, List<ValueSetExpansionContainsComponent> contains) {
		for (ValueSetExpansionContainsComponent containsComponent: contains) {
			if (containsComponent.getSystem().equals(code.getSystem()) && containsComponent.getCode().equals(code.getCode())) {
				ConceptDefinitionComponent ccd = new ConceptDefinitionComponent();
				ccd.setCode(containsComponent.getCode());
				ccd.setDisplay(containsComponent.getDisplay());
				ValidationResult res = new ValidationResult(code.getSystem(), ccd, getPreferredDisplay(ccd, null));
				return res;
			}
			if (containsComponent.hasContains()) {
				ValidationResult res = findCodeInExpansion(code, containsComponent.getContains());
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	private boolean checkExpansion(Coding code) {
		if (valueset==null || !valueset.hasExpansion()) {
			return false;
		}
		return checkExpansion(code, valueset.getExpansion().getContains());
	}

	private boolean checkExpansion(Coding code, List<ValueSetExpansionContainsComponent> contains) {
		for (ValueSetExpansionContainsComponent containsComponent: contains) {
			if (containsComponent.getSystem().equals(code.getSystem()) && containsComponent.getCode().equals(code.getCode())) {
				return true;
			}
			if (containsComponent.hasContains() && checkExpansion(code, containsComponent.getContains())) {
				return true;
			}
		}
		return false;
	}

	private ValidationResult validateCode(String path, Coding code, CodeSystem cs) {
		ConceptDefinitionComponent cc = cs.hasUserData("tx.cs.special") ? ((SpecialCodeSystem) cs.getUserData("tx.cs.special")).findConcept(code) : findCodeInConcept(cs.getConcept(), code.getCode());
		if (cc == null) {
			if (cs.getContent() == CodeSystemContentMode.FRAGMENT) {
				String msg = context.formatMessage(I18nConstants.UNKNOWN_CODE__IN_FRAGMENT, code.getCode(), cs.getUrl());
				return new ValidationResult(IssueSeverity.WARNING, msg, makeIssue(IssueSeverity.ERROR, IssueType.INVALID, path+".code", msg));
			} else {
				String msg = context.formatMessage(I18nConstants.UNKNOWN_CODE__IN_, code.getCode(), cs.getUrl());
				return new ValidationResult(IssueSeverity.ERROR, msg, makeIssue(IssueSeverity.ERROR, IssueType.INVALID, path+".code", msg));
			}
		}
		if (code.getDisplay() == null) {
			return new ValidationResult(code.getSystem(), cc, getPreferredDisplay(cc, cs));
		}
		CommaSeparatedStringBuilder b = new CommaSeparatedStringBuilder();
		if (cc.hasDisplay() && isOkLanguage(cs.getLanguage())) {
			b.append(cc.getDisplay());
			if (code.getDisplay().equalsIgnoreCase(cc.getDisplay())) {
				return new ValidationResult(code.getSystem(), cc, getPreferredDisplay(cc, cs));
			}
		}
		for (ConceptDefinitionDesignationComponent ds : cc.getDesignation()) {
			if (isOkLanguage(ds.getLanguage())) {
				b.append(ds.getValue());
				if (code.getDisplay().equalsIgnoreCase(ds.getValue())) {
					return new ValidationResult(code.getSystem(), cc, getPreferredDisplay(cc, cs));
				}
			}
		}
		// also check to see if the value set has another display
		if (options.isUseValueSetDisplays()) {
			ConceptReferencePair vs = findValueSetRef(code.getSystem(), code.getCode());
			if (vs != null && (vs.getCc().hasDisplay() ||vs.getCc().hasDesignation())) {
				if (vs.getCc().hasDisplay() && isOkLanguage(vs.getValueset().getLanguage())) {
					b.append(vs.getCc().getDisplay());
					if (code.getDisplay().equalsIgnoreCase(vs.getCc().getDisplay())) {
						return new ValidationResult(code.getSystem(), cc, getPreferredDisplay(cc, cs));
					}
				}
				for (ConceptReferenceDesignationComponent ds : vs.getCc().getDesignation()) {
					if (isOkLanguage(ds.getLanguage())) {
						b.append(ds.getValue());
						if (code.getDisplay().equalsIgnoreCase(ds.getValue())) {
							return new ValidationResult(code.getSystem(), cc, getPreferredDisplay(cc, cs));
						}
					}
				}
			}
		}
		if (b.count() == 0) {
			String msg = context.formatMessagePlural(options.getLanguages().size(), I18nConstants.NO_VALID_DISPLAY_FOUND, code.getSystem(), code.getCode(), code.getDisplay(), options.langSummary());
			return new ValidationResult(IssueSeverity.WARNING, msg, code.getSystem(), cc, getPreferredDisplay(cc, cs), makeIssue(IssueSeverity.WARNING, IssueType.INVALID, path+".display", msg));
		} else {
			String msg = context.formatMessagePlural(b.count(), I18nConstants.DISPLAY_NAME_FOR__SHOULD_BE_ONE_OF__INSTEAD_OF, code.getSystem(), code.getCode(), b.toString(), code.getDisplay(), options.langSummary());
			return new ValidationResult(IssueSeverity.WARNING, msg, code.getSystem(), cc, getPreferredDisplay(cc, cs), makeIssue(IssueSeverity.WARNING, IssueType.INVALID, path+".display", msg));
		}
	}

	private boolean isOkLanguage(String language) {
		if (!options.hasLanguages()) {
			return true;
		}
		if (options.getLanguages().contains(language)) {
			return true;
		}
		if (language == null && (options.getLanguages().contains("en") || options.getLanguages().contains("en-US") || options.isEnglishOk())) {
			return true;
		}
		return false;
	}

	private ConceptReferencePair findValueSetRef(String system, String code) {
		if (valueset == null)
			return null;
		// if it has an expansion
		for (ValueSetExpansionContainsComponent exp : valueset.getExpansion().getContains()) {
			if (system.equals(exp.getSystem()) && code.equals(exp.getCode())) {
				ConceptReferenceComponent cc = new ConceptReferenceComponent();
				cc.setDisplay(exp.getDisplay());
				cc.setDesignation(exp.getDesignation());
				return new ConceptReferencePair(valueset, cc);
			}
		}
		for (ConceptSetComponent inc : valueset.getCompose().getInclude()) {
			if (system.equals(inc.getSystem())) {
				for (ConceptReferenceComponent cc : inc.getConcept()) {
					if (cc.getCode().equals(code)) {
						return new ConceptReferencePair(valueset, cc);
					}
				}
			}
			for (CanonicalType url : inc.getValueSet()) {
				ConceptReferencePair cc = getVs(url.asStringValue()).findValueSetRef(system, code);
				if (cc != null) {
					return cc;
				}
			}
		}
		return null;
	}

	private String gen(Coding code) {
		if (code.hasSystem()) {
			return code.getSystem()+"#"+code.getCode();
		} else {
			return null;
		}
	}


	private String getValueSetSystemOrNull() throws FHIRException {
		if (valueset == null) {
			return null;
		}
		if (valueset.getCompose().getInclude().size() == 0) {
			if (!valueset.hasExpansion() || valueset.getExpansion().getContains().size() == 0) {
				return null;
			} else {
				String cs = valueset.getExpansion().getContains().get(0).getSystem();
				if (cs != null && checkSystem(valueset.getExpansion().getContains(), cs)) {
					return cs;
				} else {
					return null;
				}
			}
		}
		for (ConceptSetComponent inc : valueset.getCompose().getInclude()) {
			if (inc.hasValueSet()) {
				return null;
			}
			if (!inc.hasSystem()) {
				return null;
			}
		}
		if (valueset.getCompose().getInclude().size() == 1) {
			return valueset.getCompose().getInclude().get(0).getSystem();
		}

		return null;
	}

	/*
	 * Check that all system values within an expansion correspond to the specified system value
	 */
	private boolean checkSystem(List<ValueSetExpansionContainsComponent> containsList, String system) {
		for (ValueSetExpansionContainsComponent contains : containsList) {
			if (!contains.getSystem().equals(system) || (contains.hasContains() && !checkSystem(contains.getContains(), system))) {
				return false;
			}
		}
		return true;
	}

	private ConceptDefinitionComponent findCodeInConcept(ConceptDefinitionComponent concept, String code) {
		if (code.equals(concept.getCode())) {
			return concept;
		}
		ConceptDefinitionComponent cc = findCodeInConcept(concept.getConcept(), code);
		if (cc != null) {
			return cc;
		}
		if (concept.hasUserData(CodeSystemUtilities.USER_DATA_CROSS_LINK)) {
			List<ConceptDefinitionComponent> children = (List<ConceptDefinitionComponent>) concept.getUserData(CodeSystemUtilities.USER_DATA_CROSS_LINK);
			for (ConceptDefinitionComponent c : children) {
				cc = findCodeInConcept(c, code);
				if (cc != null) {
					return cc;
				}
			}
		}
		return null;
	}

	private ConceptDefinitionComponent findCodeInConcept(List<ConceptDefinitionComponent> concept, String code) {
		for (ConceptDefinitionComponent cc : concept) {
			if (code.equals(cc.getCode())) {
				return cc;
			}
			ConceptDefinitionComponent c = findCodeInConcept(cc, code);
			if (c != null) {
				return c;
			}
		}
		return null;
	}


	private String systemForCodeInValueSet(String code, List<String> problems) {
		Set<String> sys = new HashSet<>();
		if (!scanForCodeInValueSet(code, sys, problems)) {
			return null;
		}
		if (sys.size() != 1) {
			problems.add(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_SYSTEM__VALUE_SET_HAS_MULTIPLE_MATCHES, sys.toString()));
			return null;
		} else {
			return sys.iterator().next();
		}
	}

	private boolean scanForCodeInValueSet(String code, Set<String> sys, List<String> problems) {
		if (valueset.hasCompose()) {
			//  ignore excludes - they can't make any difference
			if (!valueset.getCompose().hasInclude() && !valueset.getExpansion().hasContains()) {
				problems.add(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_SYSTEM__VALUE_SET_HAS_NO_INCLUDES_OR_EXPANSION, valueset.getVersionedUrl()));
			}

			int i = 0;
			for (ConceptSetComponent vsi : valueset.getCompose().getInclude()) {
				if (vsi.hasValueSet()) {
					for (CanonicalType u : vsi.getValueSet()) {
						if (!checkForCodeInValueSet(code, u.getValue(), sys, problems)) {
							return false;
						}
					}
				} else if (!vsi.hasSystem()) {
					problems.add(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_SYSTEM__VALUE_SET_HAS_INCLUDE_WITH_NO_SYSTEM, valueset.getVersionedUrl(), i));
					return false;
				}
				if (vsi.hasSystem()) {
					if (vsi.hasFilter()) {
						problems.add(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_SYSTEM__VALUE_SET_HAS_INCLUDE_WITH_NO_SYSTEM, valueset.getVersionedUrl(), i, vsi.getSystem()));
						return false;
					}
					CodeSystemProvider csp = CodeSystemProvider.factory(vsi.getSystem());
					if (csp != null) {
						Boolean ok = csp.checkCode(code);
						if (ok == null) {
							problems.add(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_SYSTEM_SYSTEM_IS_INDETERMINATE, valueset.getVersionedUrl(), vsi.getSystem()));
							sys.add(vsi.getSystem());
						} else if (ok) {
							sys.add(vsi.getSystem());
						}
					} else {
						CodeSystem cs = resolveCodeSystem(vsi.getSystem(), vsi.getVersion());
						if (cs != null && cs.getContent() == CodeSystemContentMode.COMPLETE) {

							if (vsi.hasConcept()) {
								for (ConceptReferenceComponent cc : vsi.getConcept()) {
									boolean match = cs.getCaseSensitive() ? cc.getCode().equals(code) : cc.getCode().equalsIgnoreCase(code);
									if (match) {
										sys.add(vsi.getSystem());
									}
								}
							} else {
								ConceptDefinitionComponent cc = findCodeInConcept(cs.getConcept(), code);
								if (cc != null) {
									sys.add(vsi.getSystem());
								}
							}
						} else if (vsi.hasConcept()) {
							for (ConceptReferenceComponent cc : vsi.getConcept()) {
								boolean match = cc.getCode().equals(code);
								if (match) {
									sys.add(vsi.getSystem());
								}
							}
						} else {
							// we'll try to expand this one then
							ValueSetExpansionOutcome vse = context.expandVS(vsi, false, false);
							if (vse.isOk()) {
								if (!checkSystems(vse.getValueset().getExpansion().getContains(), code, sys, problems)) {
									return false;
								}
							} else {
								problems.add(context.formatMessage(I18nConstants.UNABLE_TO_RESOLVE_SYSTEM__VALUE_SET_HAS_INCLUDE_WITH_UNKNOWN_SYSTEM, valueset.getVersionedUrl(), i, vsi.getSystem(), vse.getAllErrors().toString()));
								return false;
							}
						}
					}
				}
				i++;
			}
		} else if (valueset.hasExpansion()) {
			// Retrieve a list of all systems associated with this code in the expansion
			if (!checkSystems(valueset.getExpansion().getContains(), code, sys, problems)) {
				return false;
			}
		}
		return true;
	}

	private boolean checkForCodeInValueSet(String code, String uri, Set<String> sys, List<String> problems) {
		ValueSetCheckerSimple vs = getVs(uri);
		return vs.scanForCodeInValueSet(code, sys, problems);
	}

	/*
	 * Recursively go through all codes in the expansion and for any coding that matches the specified code, add the system for that coding
	 * to the passed list.
	 */
	private boolean checkSystems(List<ValueSetExpansionContainsComponent> contains, String code, Set<String> systems, List<String> problems) {
		for (ValueSetExpansionContainsComponent c: contains) {
			if (c.getCode().equals(code)) {
				systems.add(c.getSystem());
			}
			if (c.hasContains())
				checkSystems(c.getContains(), code, systems, problems);
		}
		return true;
	}

	@Override
	public Boolean codeInValueSet(String system, String version, String code, ValidationProcessInfo info) throws FHIRException {
		return codeInValueSet("code", system, version, code, info);
	}
	public Boolean codeInValueSet(String path, String system, String version, String code, ValidationProcessInfo info) throws FHIRException {
		if (valueset == null) {
			return false;
		}
		Boolean result = false;

		if (valueset.hasExpansion()) {
			return checkExpansion(new Coding(system, code, null));
		} else if (valueset.hasCompose()) {
			int i = 0;
			for (ConceptSetComponent vsi : valueset.getCompose().getInclude()) {
				Boolean ok = inComponent(path, vsi, i, system, version, code, valueset.getCompose().getInclude().size() == 1, info);
				i++;
				if (ok == null && result == false) {
					result = null;
				} else if (ok) {
					result = true;
					break;
				}
			}
			i = valueset.getCompose().getInclude().size();
			for (ConceptSetComponent vsi : valueset.getCompose().getExclude()) {
				Boolean nok = inComponent(path, vsi, i, system, version, code, valueset.getCompose().getInclude().size() == 1, info);
				i++;
				if (nok == null && result == false) {
					result = null;
				} else if (nok != null && nok) {
					result = false;
				}
			}
		}

		return result;
	}

	private Boolean inComponent(String path, ConceptSetComponent vsi, int vsiIndex, String system, String version, String code, boolean only, ValidationProcessInfo info) throws FHIRException {
		boolean ok = true;

		if (vsi.hasValueSet()) {
			if (isValueSetUnionImports()) {
				ok = false;
				for (UriType uri : vsi.getValueSet()) {
					if (inImport(uri.getValue(), system, version, code)) {
						return true;
					}
				}
			} else {
				ok = inImport(vsi.getValueSet().get(0).getValue(), system, version, code);
				for (int i = 1; i < vsi.getValueSet().size(); i++) {
					UriType uri = vsi.getValueSet().get(i);
					ok = ok && inImport(uri.getValue(), system, version, code);
				}
			}
		}

		if (!vsi.hasSystem() || !ok) {
			return ok;
		}

		if (only && system == null) {
			// whether we know the system or not, we'll accept the stated codes at face value
			for (ConceptReferenceComponent cc : vsi.getConcept()) {
				if (cc.getCode().equals(code)) {
					return true;
				}
			}
		}

		if (system == null || !system.equals(vsi.getSystem()))
			return false;
		// ok, we need the code system
		CodeSystem cs = resolveCodeSystem(system, version);
		if (cs == null || (cs.getContent() != CodeSystemContentMode.COMPLETE && cs.getContent() != CodeSystemContentMode.FRAGMENT)) {
			// make up a transient value set with
			ValueSet vs = new ValueSet();
			vs.setStatus(PublicationStatus.ACTIVE);
			vs.setUrl(valueset.getUrl()+"--"+vsiIndex);
			vs.setVersion(valueset.getVersion());
			vs.getCompose().addInclude(vsi);
			ValidationResult res = context.validateCode(options.withNoClient(), new Coding(system, code, null), vs);
			if (res.getErrorClass() == TerminologyServiceErrorClass.UNKNOWN || res.getErrorClass() == TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED || res.getErrorClass() == TerminologyServiceErrorClass.VALUESET_UNSUPPORTED) {
				if (info != null && res.getErrorClass() == TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED) {
					// server didn't know the code system either - we'll take it face value
					info.addIssue(makeIssue(IssueSeverity.WARNING, IssueType.UNKNOWN, path, context.formatMessage(I18nConstants.TERMINOLOGY_TX_SYSTEM_NOTKNOWN, system)));
					for (ConceptReferenceComponent cc : vsi.getConcept()) {
						if (cc.getCode().equals(code)) {
							return true;
						}
					}
					info.setErr(TerminologyServiceErrorClass.CODESYSTEM_UNSUPPORTED);
				}
				return false;
			}
			if (res.getErrorClass() == TerminologyServiceErrorClass.NOSERVICE) {
				throw new NoTerminologyServiceException();
			}
			return res.isOk();
		} else {
			if (vsi.hasFilter()) {
				ok = true;
				for (ConceptSetFilterComponent f : vsi.getFilter()) {
					if (!codeInFilter(cs, system, f, code)) {
						return false;
					}
				}
			}

			List<ConceptDefinitionComponent> list = cs.getConcept();
			ok = validateCodeInConceptList(code, cs, list);
			if (ok && vsi.hasConcept()) {
				for (ConceptReferenceComponent cc : vsi.getConcept()) {
					if (cc.getCode().equals(code)) {
						return true;
					}
				}
				return false;
			} else {
				return ok;
			}
		}
	}

	protected boolean isValueSetUnionImports() {
		PackageInformation p = (PackageInformation) valueset.getSourcePackage();
		if (p != null) {
			return p.getDate().before(new GregorianCalendar(2022, Calendar.MARCH, 31).getTime());
		} else {
			return false;
		}
	}

	private boolean codeInFilter(CodeSystem cs, String system, ConceptSetFilterComponent f, String code) throws FHIRException {
		if ("concept".equals(f.getProperty()))
			return codeInConceptFilter(cs, f, code);
		else if ("code".equals(f.getProperty()) && f.getOp() == FilterOperator.REGEX)
			return codeInRegexFilter(cs, f, code);
		else {
			System.out.println("todo: handle filters with property = "+f.getProperty()+" "+f.getOp().toCode());
			throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_HANDLE_SYSTEM__FILTER_WITH_PROPERTY__, cs.getUrl(), f.getProperty(), f.getOp().toCode()));
		}
	}

	private boolean codeInRegexFilter(CodeSystem cs, ConceptSetFilterComponent f, String code) {
		return code.matches(f.getValue());
	}

	private boolean codeInConceptFilter(CodeSystem cs, ConceptSetFilterComponent f, String code) throws FHIRException {
		switch (f.getOp()) {
			case ISA: return codeInConceptIsAFilter(cs, f, code, false);
			case ISNOTA: return !codeInConceptIsAFilter(cs, f, code, false);
			case DESCENDENTOF: return codeInConceptIsAFilter(cs, f, code, true);
			default:
				System.out.println("todo: handle concept filters with op = "+f.getOp());
				throw new FHIRException(context.formatMessage(I18nConstants.UNABLE_TO_HANDLE_SYSTEM__CONCEPT_FILTER_WITH_OP__, cs.getUrl(), f.getOp()));
		}
	}

	private boolean codeInConceptIsAFilter(CodeSystem cs, ConceptSetFilterComponent f, String code, boolean rootOnly) {
		if (!rootOnly && code.equals(f.getProperty())) {
			return true;
		}
		ConceptDefinitionComponent cc = findCodeInConcept(cs.getConcept(), f.getValue());
		if (cc == null) {
			return false;
		}
		cc = findCodeInConcept(cc, code);
		return cc != null;
	}

	public boolean validateCodeInConceptList(String code, CodeSystem def, List<ConceptDefinitionComponent> list) {
		if (def.getCaseSensitive()) {
			for (ConceptDefinitionComponent cc : list) {
				if (cc.getCode().equals(code)) {
					return true;
				}
				if (cc.hasConcept() && validateCodeInConceptList(code, def, cc.getConcept())) {
					return true;
				}
			}
		} else {
			for (ConceptDefinitionComponent cc : list) {
				if (cc.getCode().equalsIgnoreCase(code)) {
					return true;
				}
				if (cc.hasConcept() && validateCodeInConceptList(code, def, cc.getConcept())) {
					return true;
				}
			}
		}
		return false;
	}

	private ValueSetCheckerSimple getVs(String url) {
		if (inner.containsKey(url)) {
			return inner.get(url);
		}
		ValueSet vs = context.fetchResource(ValueSet.class, url, valueset);
		ValueSetCheckerSimple vsc = new ValueSetCheckerSimple(options, vs, context, localContext);
		inner.put(url, vsc);
		return vsc;
	}

	private boolean inImport(String uri, String system, String version, String code) throws FHIRException {
		ValueSetCheckerSimple vs = getVs(uri);
		if (vs == null) {
			return false;
		} else {
			Boolean ok = vs.codeInValueSet(system, version, code, null);
			return ok != null && ok;
		}
	}


	private String getPreferredDisplay(ConceptReferenceComponent cc) {
		if (!options.hasLanguages()) {
			return cc.getDisplay();
		}
		if (options.getLanguages().contains(valueset.getLanguage())) {
			return cc.getDisplay();
		}
		// if there's no language, we default to accepting the displays as (US) English
		if (valueset.getLanguage() == null && (options.getLanguages().contains("en") || options.getLanguages().contains("en-US"))) {
			return cc.getDisplay();
		}
		for (ConceptReferenceDesignationComponent d : cc.getDesignation()) {
			if (!d.hasUse() && options.getLanguages().contains(d.getLanguage())) {
				return d.getValue();
			}
		}
		for (ConceptReferenceDesignationComponent d : cc.getDesignation()) {
			if (options.getLanguages().contains(d.getLanguage())) {
				return d.getValue();
			}
		}
		return cc.getDisplay();
	}


	private String getPreferredDisplay(ConceptDefinitionComponent cc, CodeSystem cs) {
		if (!options.hasLanguages()) {
			return cc.getDisplay();
		}
		if (cs != null && options.getLanguages().contains(cs.getLanguage())) {
			return cc.getDisplay();
		}
		// if there's no language, we default to accepting the displays as (US) English
		if ((cs == null || cs.getLanguage() == null) && (options.getLanguages().contains("en") || options.getLanguages().contains("en-US"))) {
			return cc.getDisplay();
		}
		for (ConceptDefinitionDesignationComponent d : cc.getDesignation()) {
			if (!d.hasUse() && options.getLanguages().contains(d.getLanguage())) {
				return d.getValue();
			}
		}
		for (ConceptDefinitionDesignationComponent d : cc.getDesignation()) {
			if (options.getLanguages().contains(d.getLanguage())) {
				return d.getValue();
			}
		}
		return cc.getDisplay();
	}

}
