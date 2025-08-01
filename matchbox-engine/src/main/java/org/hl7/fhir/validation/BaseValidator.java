package org.hl7.fhir.validation;


import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

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

import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_10_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_14_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_30_50;
import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.JsonParser;
import org.hl7.fhir.r5.extensions.ExtensionDefinitions;
import org.hl7.fhir.r5.extensions.ExtensionUtilities;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.CanonicalResource;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Constants;
import org.hl7.fhir.r5.model.DomainResource;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.Extension;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.UsageContext;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.r5.model.Enumerations.PublicationStatus;
import org.hl7.fhir.r5.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r5.terminologies.ImplicitValueSets;
import org.hl7.fhir.r5.utils.UserDataNames;
import org.hl7.fhir.r5.utils.XVerExtensionManager;
import org.hl7.fhir.r5.utils.XVerExtensionManager.XVerExtensionStatus;
import org.hl7.fhir.r5.utils.validation.IMessagingServices;
import org.hl7.fhir.r5.utils.validation.IValidationPolicyAdvisor;
import org.hl7.fhir.r5.utils.validation.IValidatorResourceFetcher;
import org.hl7.fhir.r5.utils.validation.ValidatorSession;
import org.hl7.fhir.r5.utils.validation.ValidationContextCarrier.IValidationContextResourceLoader;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.utilities.*;
import org.hl7.fhir.utilities.i18n.I18nConstants;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueType;
import org.hl7.fhir.utilities.validation.ValidationMessage.Source;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.hl7.fhir.validation.service.utils.ValidationLevel;
import org.hl7.fhir.validation.instance.InstanceValidator;
import org.hl7.fhir.validation.instance.advisor.BasePolicyAdvisorForFullValidation;
import org.hl7.fhir.validation.instance.utils.IndexedElement;
import org.hl7.fhir.validation.instance.utils.NodeStack;

public class BaseValidator implements IValidationContextResourceLoader, IMessagingServices {

  /**
   * This regex tests FHIR search parameters. It expects the formats:
   * [paramName]=[paramValue]
   * [paramName]=[paramValue]&[paramName]=[paramValue]
   * etc..
   */
  private static final Pattern SEARCH_URL_PARAMS = Pattern.compile("[_a-zA-Z][_a-zA-Z0-9.:-]*=[^=&]*(&([_a-zA-Z][_a-zA-Z0-9.:]*=[^=&]*))*");

  public static class BooleanHolder {
    private boolean value = true;

    public BooleanHolder() {
      super();
      this.value = true;
    }
    public BooleanHolder(boolean value) {
      super();
      this.value = value;
    }
    public void fail() {
      value = false;
    }
    public boolean ok() {
      return value;
    }
    public void see(boolean ok) {
      value = value && ok;
    }
    public void set(boolean value) {
      this.value = value;
    }
  }
  

  public class TrackedLocationRelatedMessage {
    private Object location;
    private ValidationMessage vmsg;
    public TrackedLocationRelatedMessage(Object location, ValidationMessage vmsg) {
      super();
      this.location = location;
      this.vmsg = vmsg;
    }
    public Object getLocation() {
      return location;
    }
    public ValidationMessage getVmsg() {
      return vmsg;
    }
      }

  public class ValidationControl {
    private boolean allowed;
    private IssueSeverity level;
    
    public ValidationControl(boolean allowed, IssueSeverity level) {
      super();
      this.allowed = allowed;
      this.level = level;
    }
    public boolean isAllowed() {
      return allowed;
    }
    public IssueSeverity getLevel() {
      return level;
    }
  }

  public static final String NO_RULE_DATE = ValidationMessage.NO_RULE_DATE;

  protected final String META = "meta";
  protected final String ENTRY = "entry";
  protected final String LINK = "link";
  protected final String DOCUMENT = "document";
  protected final String RESOURCE = "resource";
  protected final String MESSAGE = "message";
  protected final String SEARCHSET = "searchset";
  protected final String ID = "id";
  protected final String FULL_URL = "fullUrl";
  protected final String PATH_ARG = ":0";
  protected final String TYPE = "type";
  protected final String BUNDLE = "Bundle";
  protected final String LAST_UPDATED = "lastUpdated";
  protected final String VERSION_ID = "versionId";

  protected BaseValidator parent;
  protected IWorkerContext context;
  protected ValidationTimeTracker timeTracker = new ValidationTimeTracker();
  protected XVerExtensionManager xverManager;
  protected IValidatorResourceFetcher fetcher;
  protected IValidationPolicyAdvisor policyAdvisor;
  protected boolean noTerminologyChecks;
  protected ValidatorSettings settings;
  
  // these two related to removing warnings on extensible bindings in structures that have derivatives that replace their bindings
  protected List<TrackedLocationRelatedMessage> trackedMessages = new ArrayList<>();   
  protected List<ValidationMessage> messagesToRemove = new ArrayList<>();

  // don't repeatedly raise the same warnings all the time
  protected Set<String> statusWarnings = new HashSet<>();  
  
  protected ValidatorSession session;
  protected ContextUtilities cu;

  public BaseValidator(IWorkerContext context, @Nonnull ValidatorSettings settings, XVerExtensionManager xverManager, ValidatorSession session) {
    super();
    this.context = context;
    cu = new ContextUtilities(context);
    this.session = session;
    if (this.session == null) {
      this.session = new ValidatorSession();
    }
    this.xverManager = xverManager;
    if (this.xverManager == null) {
      this.xverManager = new XVerExtensionManager(context);
    }
    this.settings = settings;
    policyAdvisor = new BasePolicyAdvisorForFullValidation(ReferenceValidationPolicy.CHECK_VALID);
    urlRegex = Constants.URI_REGEX_XVER.replace("$$", CommaSeparatedStringBuilder.join("|", context.getResourceNames()));
  }
  
  public BaseValidator(BaseValidator parent) {
    super();
    this.parent = parent;
    this.session = parent.session;
    this.context = parent.context;
    this.cu = parent.cu;
    this.xverManager = parent.xverManager;
    this.timeTracker = parent.timeTracker;
    this.trackedMessages = parent.trackedMessages;
    this.messagesToRemove = parent.messagesToRemove;
    this.statusWarnings = parent.statusWarnings;
    this.urlRegex = parent.urlRegex;
    this.settings = parent.settings;
    this.fetcher = parent.fetcher;
    this.policyAdvisor = parent.policyAdvisor;
    this.noTerminologyChecks = parent.noTerminologyChecks;
  }
  
  private boolean doingLevel(IssueSeverity error) {
    switch (error) {
    case ERROR:
      return settings.getLevel() == null || settings.getLevel() == ValidationLevel.ERRORS || settings.getLevel() == ValidationLevel.WARNINGS || settings.getLevel() == ValidationLevel.HINTS;
    case FATAL:
      return settings.getLevel() == null || settings.getLevel() == ValidationLevel.ERRORS || settings.getLevel() == ValidationLevel.WARNINGS || settings.getLevel() == ValidationLevel.HINTS;
    case WARNING:
      return settings.getLevel() == null || settings.getLevel() == ValidationLevel.WARNINGS || settings.getLevel() == ValidationLevel.HINTS;
    case INFORMATION:
      return settings.getLevel() == null || settings.getLevel() == ValidationLevel.HINTS;
    case NULL:
      return true;
    default:
      return true;    
    }
  }

  private boolean doingErrors() {
    return doingLevel(IssueSeverity.ERROR);
  }
  
  private boolean doingWarnings() {
    return doingLevel(IssueSeverity.WARNING);
  }
  
  private boolean doingHints() {
    return doingLevel(IssueSeverity.INFORMATION);
  }
  

  /**
   * Use to control what validation the validator performs. 
   * Using this, you can turn particular kinds of validation on and off 
   * In addition, you can override the error | warning | hint level and make it a different level
   * 
   * There is no way to do this using the command line validator; it's a service that is only 
   * offered when the validator is hosted in some other process
   */
  private Map<String, ValidationControl> validationControl = new HashMap<>();

  protected String urlRegex;

  protected boolean isSuppressedValidationMessage(String path, String theMessage) {
    if (policyAdvisor == null) {
      return false;
    } else {
      return policyAdvisor.isSuppressMessageId(path, theMessage);
    }
  }

  protected boolean fail(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(path, theMessage)) {
      String msg = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, line, col, path, msg, IssueSeverity.FATAL, theMessage);
    }
    return thePass;
  }

  //TODO: i18n
  protected boolean grammarWord(String w) {
    return w.equals("and") || w.equals("or") || w.equals("a") || w.equals("the") || w.equals("for") || w.equals("this") || w.equals("that") || w.equals("of");
  }

  /**
   * Test a rule and add a {@link IssueSeverity#INFORMATION} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean hint(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String msg) {
    if (!thePass && doingHints() && !isSuppressedValidationMessage(path, msg)) {
      String message = context.formatMessage(msg);
      addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.INFORMATION, msg);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#INFORMATION} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean hintInv(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String msg, String invId, String msgId) {
    if (!thePass && doingHints() && !isSuppressedValidationMessage(path, invId)) {
      String message = context.formatMessage(msg);
      addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.INFORMATION, msg).setInvId(invId).setMessageId(msgId);
    }
    return thePass;
  }

  protected boolean hint(List<ValidationMessage> errors, String ruleDate, IssueType type, NodeStack stack, boolean thePass, String msg, Object... theMessageArguments) {
    return hint(errors, ruleDate, type, stack.line(), stack.col(), stack.getLiteralPath(),  thePass, msg, theMessageArguments);
  }

  protected boolean slicingHint(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, boolean isCritical, String msg, String html, List<ValidationMessage> info) {
    if (!thePass && doingHints()) {
      addValidationMessage(errors, ruleDate, type, line, col, path, msg, IssueSeverity.INFORMATION, null)
           .setMessageId(I18nConstants.DETAILS_FOR__MATCHING_AGAINST_PROFILE_).setSlicingHint(true).setSliceHtml(html, info).setCriticalSignpost(isCritical);
    }
    return thePass;
  }
  /**
   * Test a rule and add a {@link IssueSeverity#INFORMATION} validation message if the validation fails. And mark it as a slicing hint for later recovery if appropriate
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean slicingHint(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, boolean isCritical, String msg, String html, List<ValidationMessage> info, String id) {
    if (!thePass && doingHints()) {
      addValidationMessage(errors, ruleDate, type, line, col, path, msg, IssueSeverity.INFORMATION, id)
      .setMessageId(I18nConstants.DETAILS_FOR__MATCHING_AGAINST_PROFILE_).setSlicingHint(true).setSliceHtml(html, info).setCriticalSignpost(isCritical);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#INFORMATION} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean hint(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingHints() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.INFORMATION, theMessage);
    }
    return thePass;
  }

  protected boolean hintPlural(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, int num, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingHints() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessagePlural(num, theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.INFORMATION, theMessage);
    }
    return thePass;
  }

  public ValidationMessage signpost(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, String theMessage, Object... theMessageArguments) {
    String message = context.formatMessage(theMessage, theMessageArguments);
    return addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.INFORMATION, theMessage).setSignpost(true);
  }

  protected boolean txHint(List<ValidationMessage> errors, String ruleDate, String txLink, IssueType type, int line, int col, String path, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingHints() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.INFORMATION, Source.TerminologyEngine, theMessage).setTxLink(txLink);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#INFORMATION} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean hint(List<ValidationMessage> errors, String ruleDate, IssueType type, List<String> pathParts, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingHints() && !isSuppressedValidationMessage(CommaSeparatedStringBuilder.join(".", pathParts), theMessage)) {
      String path = toPath(pathParts);
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.INFORMATION, theMessage);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#INFORMATION} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean hint(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingHints() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.INFORMATION, null);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#ERROR} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean rule(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.ERROR, theMessage);
    }
    return thePass;
  }

  protected boolean ruleInv(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String theMessage, String invId, String msgId, Object... theMessageArguments) {
    // matchbox patch, otherwise the message text instead of the invId is used
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(path, invId)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.ERROR, invId).setInvId(invId).setMessageId(msgId);
    }
    return thePass;
  }

  protected boolean rule(List<ValidationMessage> errors, String ruleDate, IssueType type, NodeStack stack, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(stack.getLiteralPath(), theMessage)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, stack.line(), stack.col(), stack.getLiteralPath(), message, IssueSeverity.ERROR, theMessage);
    }
    return thePass;
  }

  protected boolean rulePlural(List<ValidationMessage> errors, String ruleDate, IssueType type, NodeStack node, boolean thePass, int num, String theMessage, Object... theMessageArguments) {
    return rulePlural(errors, ruleDate, type, node.line(), node.col(), node.getLiteralPath(), thePass, num, theMessage, theMessageArguments);
  }
  
  protected boolean rulePlural(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, int num, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessagePlural(num, theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, line, col, path, message, IssueSeverity.ERROR, theMessage);
    }
    return thePass;
  }

  protected boolean txRule(List<ValidationMessage> errors, String ruleDate, String txLink, IssueType type, int line, int col, String path, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      ValidationMessage vm = new ValidationMessage(Source.TerminologyEngine, type, line, col, path, message, IssueSeverity.ERROR).setMessageId(idForMessage(theMessage, message));
      vm.setRuleDate(ruleDate);
      if (checkMsgId(theMessage, vm)) {
        errors.add(vm.setTxLink(txLink));
      }
    }
    return thePass;
  }

  private String idForMessage(String theMessage, String message) {
    return theMessage.equals(message) ? null : theMessage;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#ERROR} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean rule(List<ValidationMessage> errors, String ruleDate, IssueType type, List<String> pathParts, boolean thePass, String msg) {
    if (!thePass && doingErrors()) {
      String path = toPath(pathParts);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, msg, IssueSeverity.ERROR, null);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#ERROR} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean rule(List<ValidationMessage> errors, String ruleDate, IssueType type, List<String> pathParts, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(CommaSeparatedStringBuilder.join(".", pathParts), theMessage)) {
      String path = toPath(pathParts);
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.ERROR, theMessage);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#ERROR} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */


  protected boolean rule(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.ERROR, theMessage);
    }
    return thePass;
  }

  protected boolean rulePlural(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, int num, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessagePlural(num, theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.ERROR, theMessage);
    }
    return thePass;
  }

  public boolean rule(List<ValidationMessage> errors, String ruleDate, Source source, IssueType type, String path, boolean thePass, String msg) {
    if (!thePass && doingErrors()) {
      addValidationMessage(errors, ruleDate, type, -1, -1, path, msg, IssueSeverity.ERROR, source, null);
    }
    return thePass;
  }

  protected boolean rule(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, List<ValidationMessage> details, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingErrors() && !isSuppressedValidationMessage(path, theMessage)) {
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.ERROR, theMessage).setSliceInfo(details);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#ERROR} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean ruleHtml(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String msg, String html) {
    if (!thePass && doingErrors()) {
      msg = context.formatMessage(msg);
      html = context.formatMessage(html);
      addValidationMessage(errors, ruleDate, type, path, msg, html, IssueSeverity.ERROR, null);
    }
    return thePass;
  }

  protected String splitByCamelCase(String s) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (Character.isUpperCase(c) && !(i == 0 || Character.isUpperCase(s.charAt(i-1))))
        b.append(' ');
      b.append(c);
    }
    return b.toString();
  }

  protected String stripPunctuation(String s, boolean numbers) {
    StringBuilder b = new StringBuilder();
    for (char c : s.toCharArray()) {
      int t = Character.getType(c);
      if (t == Character.UPPERCASE_LETTER || t == Character.LOWERCASE_LETTER || t == Character.TITLECASE_LETTER || t == Character.MODIFIER_LETTER || t == Character.OTHER_LETTER || (t == Character.DECIMAL_DIGIT_NUMBER && numbers) || (t == Character.LETTER_NUMBER && numbers) || c == ' ')
        b.append(c);
    }
    return b.toString();
  }

  private String toPath(List<String> pathParts) {
    if (pathParts == null || pathParts.isEmpty()) {
      return "";
    }
    return "//" + StringUtils.join(pathParts, '/');
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean warning(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String msg, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      IssueSeverity severity = IssueSeverity.WARNING;
      addValidationMessage(errors, ruleDate, type, line, col, path, nmsg, severity, msg);
    }
    return thePass;

  }
  
  protected boolean warning(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, String id, boolean thePass, String msg, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      IssueSeverity severity = IssueSeverity.WARNING;
      addValidationMessage(errors, ruleDate, type, line, col, path, nmsg, severity, id);
    }
    return thePass;

  }
  
  protected boolean warningInv(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String msg, String invId, String msgId, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, invId)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      IssueSeverity severity = IssueSeverity.WARNING;
      String id = idForMessage(msg, nmsg);
      addValidationMessage(errors, ruleDate, type, line, col, path, nmsg, severity, id).setMessageId(msgId).setInvId(invId);
    }
    return thePass;

  }

  protected boolean warning(List<ValidationMessage> errors, String ruleDate, IssueType type, NodeStack stack, boolean thePass, String msg, Object... theMessageArguments) {
    return warning(errors, ruleDate, type, stack, null, thePass, msg, theMessageArguments);
  }
  
  protected boolean warning(List<ValidationMessage> errors, String ruleDate, IssueType type, NodeStack stack, String id, boolean thePass, String msg, Object... theMessageArguments) {
    return warning(errors, ruleDate, type, stack.line(), stack.col(), stack.getLiteralPath(), id, thePass, msg, theMessageArguments);
  }

  protected boolean warningPlural(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, int num, String msg, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessagePlural(num, msg, theMessageArguments);
      IssueSeverity severity = IssueSeverity.WARNING;
      addValidationMessage(errors, ruleDate, type, line, col, path, nmsg, severity, msg);
    }
    return thePass;

  }

  protected ValidationMessage addValidationMessage(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, String msg, IssueSeverity theSeverity, String id) {
    Source source = this.settings.getSource();
    return addValidationMessage(errors, ruleDate, type, line, col, path, msg, theSeverity, source, id);
  }

  protected ValidationMessage addValidationMessage(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, String msg, IssueSeverity theSeverity, Source theSource, String id) {
    ValidationMessage validationMessage = new ValidationMessage(theSource, type, line, col, path, msg, theSeverity).setMessageId(id);
    validationMessage.setRuleDate(ruleDate);
    if (doingLevel(theSeverity) && checkMsgId(id, validationMessage)) {
      errors.add(validationMessage);
    }
    return validationMessage;
  }

  public boolean checkMsgId(String id, ValidationMessage vm) { 
    if (id != null && validationControl.containsKey(id)) {
      ValidationControl control = validationControl.get(id);
      if (control.level != null) {
        vm.setLevel(control.level);
      }
      return control.isAllowed();
    }
    return true;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean txWarning(List<ValidationMessage> errors, String ruleDate, String txLink, IssueType type, int line, int col, String path, boolean thePass, String msg, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      ValidationMessage vmsg = new ValidationMessage(Source.TerminologyEngine, type, line, col, path, nmsg, IssueSeverity.WARNING).setTxLink(txLink).setMessageId(idForMessage(msg, nmsg));
      vmsg.setRuleDate(ruleDate);
      if (checkMsgId(msg, vmsg)) {
        errors.add(vmsg);
      }
    }
    return thePass;

  }
  
  /**
   * @param thePass Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected ValidationMessage buildValidationMessage(String txLink, int line, int col, String path, OperationOutcomeIssueComponent issue) {
    if (issue.hasLocation() && issue.getExpressionOrLocation().get(0).getValue().contains(".")) {
      path = path + dropHead(issue.getExpressionOrLocation().get(0).getValue());
    }
    IssueType code = IssueType.fromCode(issue.getCode().toCode());
    IssueSeverity severity = IssueSeverity.fromCode(issue.getSeverity().toCode());
    ValidationMessage validationMessage = new ValidationMessage(Source.TerminologyEngine, code, line, col, path, issue.getDetails().getText(), severity).setTxLink(txLink);
    if (issue.getExtensionString(ExtensionDefinitions.EXT_ISSUE_SERVER) != null) {
      validationMessage.setServer(issue.getExtensionString(ExtensionDefinitions.EXT_ISSUE_SERVER).replace("local.fhir.org", "tx-dev.fhir.org"));
    }
    if (issue.getExtensionString(ExtensionDefinitions.EXT_ISSUE_MSG_ID) != null) {
      validationMessage.setMessageId(issue.getExtensionString(ExtensionDefinitions.EXT_ISSUE_MSG_ID));
    }
    return validationMessage;
  }
  
  private String dropHead(String value) {
    return value.contains(".") ? value.substring(value.indexOf(".")) : "";
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails. Also, keep track of it later in case we want to remove it if we find a required binding for this element later
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean txWarningForLaterRemoval(Object location, List<ValidationMessage> errors, String ruleDate, String txLink, IssueType type, int line, int col, String path, boolean thePass, String msg, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      ValidationMessage vmsg = new ValidationMessage(Source.TerminologyEngine, type, line, col, path, nmsg, IssueSeverity.WARNING).setTxLink(txLink).setMessageId(msg);
      vmsg.setRuleDate(ruleDate);
      if (checkMsgId(msg, vmsg)) {
        errors.add(vmsg);
      }
      trackedMessages.add(new TrackedLocationRelatedMessage(location, vmsg));
    }
    return thePass;

  }

  protected void removeTrackedMessagesForLocation(List<ValidationMessage> errors, Object location, String path) {
    List<TrackedLocationRelatedMessage> messages = new ArrayList<>();
    for (TrackedLocationRelatedMessage m : trackedMessages) {
      if (m.getLocation() == location) {
        messages.add(m);
        messagesToRemove.add(m.getVmsg());
      }
    }
    trackedMessages.removeAll(messages);    
  }
  
  protected boolean warningOrError(boolean isError, List<ValidationMessage> errors, String ruleDate, IssueType type, NodeStack stack, boolean thePass, String msg, Object... theMessageArguments) {
    return warningOrError(isError, errors, ruleDate, type, stack.line(), stack.col(), stack.getLiteralPath(), thePass, msg, theMessageArguments);
  }
  
  protected boolean warningOrError(boolean isError, List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String msg, Object... theMessageArguments) {
    if (!thePass && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      IssueSeverity lvl = isError ? IssueSeverity.ERROR : IssueSeverity.WARNING;
      if (doingLevel(lvl)) {
        addValidationMessage(errors, ruleDate, type, line, col, path, nmsg, lvl, msg);
      }
    }
    return thePass;

  }

  protected boolean hintOrError(boolean isError, List<ValidationMessage> errors, String ruleDate, IssueType type, NodeStack stack, boolean thePass, String msg, Object... theMessageArguments) {
    return hintOrError(isError, errors, ruleDate, type, stack.line(), stack.col(), stack.getLiteralPath(), thePass, msg, theMessageArguments);
  }
  
  protected boolean hintOrError(boolean isError, List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String msg, Object... theMessageArguments) {
    if (!thePass && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      IssueSeverity lvl = isError ? IssueSeverity.ERROR : IssueSeverity.INFORMATION;
      if (doingLevel(lvl)) {
        addValidationMessage(errors, ruleDate, type, line, col, path, nmsg, lvl, msg);
      }
    }
    return thePass;

  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean warning(List<ValidationMessage> errors, String ruleDate, IssueType type, List<String> pathParts, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(CommaSeparatedStringBuilder.join(".", pathParts), theMessage)) {
      String path = toPath(pathParts);
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.WARNING, theMessage);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean warning(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String msg, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String message = context.formatMessage(msg, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.WARNING, msg);
    }
    return thePass;
  }

  protected boolean warning(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, List<ValidationMessage> details, String msg, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String message = context.formatMessage(msg, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.WARNING, msg).setSliceInfo(details);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean warningOrHint(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, boolean warning, String msg, Object... theMessageArguments) {
    if (!thePass && !isSuppressedValidationMessage(path, msg)) {
      String message = context.formatMessage(msg, theMessageArguments);
      IssueSeverity lvl = warning ? IssueSeverity.WARNING : IssueSeverity.INFORMATION;
      if  (doingLevel(lvl)) {
        addValidationMessage(errors, ruleDate, type, -1, -1, path, message, lvl, null);
      }
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean warningHtml(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String msg, String html) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      addValidationMessage(errors, ruleDate, type, path, msg, html, IssueSeverity.WARNING, null);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean warningHtml(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String msg, String html, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, path, nmsg, html, IssueSeverity.WARNING, msg);
    }
    return thePass;
  }

  //---------
  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean suppressedwarning(List<ValidationMessage> errors, String ruleDate, IssueType type, int line, int col, String path, boolean thePass, String msg, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, line, col, path, nmsg, IssueSeverity.INFORMATION, msg);
    }
    return thePass;

  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean suppressedwarning(List<ValidationMessage> errors, String ruleDate, IssueType type, List<String> pathParts, boolean thePass, String theMessage, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(CommaSeparatedStringBuilder.join(".", pathParts), theMessage)) {
      String path = toPath(pathParts);
      String message = context.formatMessage(theMessage, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, -1, -1, path, message, IssueSeverity.INFORMATION, theMessage);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean suppressedwarning(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String msg) {
    if (!thePass && doingWarnings()) {
      addValidationMessage(errors, ruleDate, type, -1, -1, path, msg, IssueSeverity.INFORMATION, null);
    }
    return thePass;
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean suppressedwarning(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String msg, String html) {
    if (!thePass && doingWarnings()) {
      IssueSeverity severity = IssueSeverity.INFORMATION;
      addValidationMessage(errors, ruleDate, type, path, msg, html, severity, null);
    }
    return thePass;
  }

  protected void addValidationMessage(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, String msg, String html, IssueSeverity theSeverity, String id) {
    ValidationMessage vm = new ValidationMessage(settings.getSource(), type, -1, -1, path, msg, html, theSeverity);
    vm.setRuleDate(ruleDate);
    if (checkMsgId(id, vm)) {
      if (doingLevel(theSeverity)) {
        errors.add(vm.setMessageId(id));
      }
    }
  }

  /**
   * Test a rule and add a {@link IssueSeverity#WARNING} validation message if the validation fails
   * 
   * @param thePass
   *          Set this parameter to <code>false</code> if the validation does not pass
   * @return Returns <code>thePass</code> (in other words, returns <code>true</code> if the rule did not fail validation)
   */
  protected boolean suppressedwarning(List<ValidationMessage> errors, String ruleDate, IssueType type, String path, boolean thePass, String msg, String html, Object... theMessageArguments) {
    if (!thePass && doingWarnings() && !isSuppressedValidationMessage(path, msg)) {
      String nmsg = context.formatMessage(msg, theMessageArguments);
      addValidationMessage(errors, ruleDate, type, path, nmsg, html, IssueSeverity.INFORMATION, msg);
    }
    return thePass;
  }


  protected ValueSet resolveBindingReference(DomainResource ctxt, String reference, String uri, Resource src) {
    if (reference != null) {
      if (reference.equals("http://www.rfc-editor.org/bcp/bcp13.txt")) {
        reference = "http://hl7.org/fhir/ValueSet/mimetypes";
      }
      if (reference.startsWith("#")) {
        for (Resource c : ctxt.getContained()) {
          if (c.getId().equals(reference.substring(1)) && (c instanceof ValueSet))
            return (ValueSet) c;
        }
        return null;
      } else {
        reference = cu.pinValueSet(reference);
        long t = System.nanoTime();
        ValueSet fr = context.findTxResource(ValueSet.class, reference, src);
        if (fr == null) {
          if (!Utilities.isAbsoluteUrl(reference)) {
            reference = resolve(uri, reference);
            fr = context.findTxResource(ValueSet.class, reference, src);
          }
        }
        if (fr == null) {
          fr = new ImplicitValueSets(context.getExpansionParameters()).generateImplicitValueSet(reference);
        } 
       
        timeTracker.tx(t, "vs "+uri);
        return fr;
      }
    } else
      return null;
  }


  private String resolve(String uri, String ref) {
    if (isBlank(uri)) {
      return ref;
    }
    String[] up = uri.split("\\/");
    String[] rp = ref.split("\\/");
    if (context.getResourceNames().contains(up[up.length - 2]) && context.getResourceNames().contains(rp[0])) {
      StringBuilder b = new StringBuilder();
      for (int i = 0; i < up.length - 2; i++) {
        b.append(up[i]);
        b.append("/");
      }
      b.append(ref);
      return b.toString();
    } else
      return ref;
  }

  protected String describeReference(String reference) {
    if (reference == null)
      return "null";
    return reference;
  }

  protected Base resolveInBundle(String url, Element bnd) {
    if (bnd == null)
      return null;
    if (bnd.fhirType().equals(BUNDLE)) {
      for (Element be : bnd.getChildrenByName(ENTRY)) {
        Element res = be.getNamedChild(RESOURCE, false);
        if (res != null) {
          String fullUrl = be.getChildValue(FULL_URL);
          String rt = res.fhirType();
          String id = res.getChildValue(ID);
          if (url.equals(fullUrl))
            return res;
          if (url.equals(rt + "/" + id))
            return res;
        }
      }
    }
    return null;
  }

  protected Element resolveInBundle(Element bundle, List<Element> entries, String ref, String fullUrl, String type, String id, NodeStack stack, List<ValidationMessage> errors, String name, Element source, boolean isWarning, boolean isNLLink) {
    @SuppressWarnings("unchecked")
    Map<String, List<Element>> map = (Map<String, List<Element>>) bundle.getUserData(UserDataNames.validator_entry_map);
    @SuppressWarnings("unchecked")
    Map<String, List<Element>> relMap = (Map<String, List<Element>>) bundle.getUserData(UserDataNames.validator_entry_map_reverse);
    List<Element> list = null;

    if (map == null) {
      map = new HashMap<>();
      bundle.setUserData(UserDataNames.validator_entry_map, map);
      relMap = new HashMap<>();
      bundle.setUserData(UserDataNames.validator_entry_map_reverse, relMap);
      for (Element entry : entries) {
        String fu = entry.getNamedChildValue(FULL_URL, false);
        list = map.get(fu);
        if (list == null) {
          list = new ArrayList<Element>();
          map.put(fu, list);
        }
        list.add(entry);
        
        Element resource = entry.getNamedChild(RESOURCE, false);
        if (resource != null) {
          String et = resource.getType();
          String eid = resource.getNamedChildValue(ID, false);
          String rl = null;
          if (eid != null) {
            rl = et+"/"+eid;
            list = relMap.get(rl);
            if (list == null) {
              list = new ArrayList<Element>();
              relMap.put(rl, list);
            }
            list.add(entry);
          }
          boolean versionIdPresent = resource.hasChild(META, false)
            && resource.getNamedChild(META, false).hasChild(VERSION_ID, false)
            && resource.getNamedChild(META, false).getNamedChild(VERSION_ID, false).hasValue();
          if (versionIdPresent){
            String versionId = resource.getNamedChild(META).getNamedChild(VERSION_ID).getValue();
            String fullUrlVersioned = fu + "/_history/" + versionId;
            List<Element> listMapVersioned = null;
            listMapVersioned = map.get(fullUrlVersioned);
            if (listMapVersioned == null) {
              listMapVersioned = new ArrayList<Element>();
              map.put(fullUrlVersioned, listMapVersioned);
            }
            listMapVersioned.add(entry);
            if (rl != null) {
              String relativePathVersioned = rl + "/_history/" + versionId;
              List<Element> listRelMapVersioned = null;
              listRelMapVersioned = relMap.get(relativePathVersioned);
              if (listRelMapVersioned == null) {
                listRelMapVersioned = new ArrayList<Element>();
                relMap.put(relativePathVersioned, listRelMapVersioned);
              }
              listRelMapVersioned.add(entry);
            }
          }
        }
      }      
    }
    
    String fragment = null;
    if (ref != null && ref.contains("#")) {
      fragment = ref.substring(ref.indexOf("#")+1);
      ref = ref.substring(0, ref.indexOf("#"));
    }
    
    if (Utilities.isAbsoluteUrl(ref)) {
      // if the reference is absolute, then you resolve by fullUrl. No other thinking is required.
      List<Element> el = map.get(ref);
      if (el == null) {
        // if this something we complain about? 
        // not if it's in a package, or it looks like a restful URL and it's one of the canonical resource types
        boolean ok = context.hasResource(Resource.class, ref);
        if (!ok && ref.matches(urlRegex)) {
          String tt = extractResourceType(ref);
          ok = VersionUtilities.getCanonicalResourceNames(context.getVersion()).contains(tt);
        }
        if (!ok && stack != null && !session.getSessionId().equals(source.getUserString(UserDataNames.validation_bundle_error))) {
          source.setUserData(UserDataNames.validation_bundle_error, session.getSessionId());          
          hintOrError(!isWarning, errors, NO_RULE_DATE, IssueType.NOTFOUND, stack, false, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOTFOUND, ref, name);
        }
        return null;
      } else if (el.size() == 1) {
        if (fragment != null) {
          int i = countFragmentMatches(el.get(0), fragment);
          if (i == 0) {
            source.setUserData(UserDataNames.validation_bundle_error, session.getSessionId());
            hintOrError(isNLLink, errors, NO_RULE_DATE, IssueType.NOTFOUND, stack, false, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOTFOUND_FRAGMENT, ref, fragment, name);            
          } else if (i > 1) {
            source.setUserData(UserDataNames.validation_bundle_error, session.getSessionId());
            rule(errors, "2023-11-15", IssueType.INVALID, stack, false, I18nConstants.BUNDLE_BUNDLE_ENTRY_FOUND_MULTIPLE_FRAGMENT, i, ref, fragment, name);            
          }
        }
        return el.get(0);
      } else {
        if (stack != null && !session.getSessionId().equals(source.getUserString(UserDataNames.validation_bundle_error))) {
          source.setUserData(UserDataNames.validation_bundle_error, session.getSessionId());
          rule(errors, "2023-11-15", IssueType.INVALID, stack, false, I18nConstants.BUNDLE_BUNDLE_ENTRY_FOUND_MULTIPLE, el.size(), ref, name);
        }
        return null;
      }
    } else {
      // split into base, type, and id
      String u = null;
      if (fullUrl != null && fullUrl.matches(urlRegex) && fullUrl.endsWith(type + "/" + id)) {
        u = fullUrl.substring(0, fullUrl.length() - (type + "/" + id).length()) + ref;
      }
      List<Element> el = map.get(u);
      if (el != null && el.size() > 0) {
        if (el.size() == 1) {
          return el.get(0);
        } else {
          if (stack != null && !session.getSessionId().equals(source.getUserString(UserDataNames.validation_bundle_error))) {
            source.setUserData(UserDataNames.validation_bundle_error, session.getSessionId());
            rule(errors, "2023-11-15", IssueType.INVALID, stack, false, I18nConstants.BUNDLE_BUNDLE_ENTRY_FOUND_MULTIPLE, el.size(), ref, name);
          }
          return null;
        }
      } else {
        String[] parts = ref.split("\\/");
        if (parts.length >= 2) {
          String t = parts[0];
          if (context.getResourceNamesAsSet().contains(t)) {
            String i = parts[1];
            el = relMap.get(t+"/"+i);
            if (el != null) {
              Set<String> tl = new HashSet<>();
              for (Element e : el) {
                String fu = e.getNamedChildValue(FULL_URL, false);
                tl.add(fu == null ? "<missing>" : fu);
              }
              if (!VersionUtilities.isR4Plus(context.getVersion())) {
                if (el.size() == 1) {
                  return el.get(0);
                } else if (stack != null && !session.getSessionId().equals(source.getUserString(UserDataNames.validation_bundle_error))) {
                  source.setUserData(UserDataNames.validation_bundle_error, session.getSessionId());
                  rulePlural(errors, "2023-11-15", IssueType.INVALID, stack, false, el.size(), I18nConstants.BUNDLE_BUNDLE_ENTRY_NOTFOUND_APPARENT, ref, name, CommaSeparatedStringBuilder.join(",", Utilities.sorted(tl)));
                }
              } else if (stack != null && !session.getSessionId().equals(source.getUserString(UserDataNames.validation_bundle_error))) {
                source.setUserData(UserDataNames.validation_bundle_error, session.getSessionId());
                rulePlural(errors, "2023-11-15", IssueType.INVALID, stack, false, el.size(), I18nConstants.BUNDLE_BUNDLE_ENTRY_NOTFOUND_APPARENT, ref, name, CommaSeparatedStringBuilder.join(",", Utilities.sorted(tl)));
              }
            } else {
              if (stack != null && !session.getSessionId().equals(source.getUserString(UserDataNames.validation_bundle_error))) {
                source.setUserData(UserDataNames.validation_bundle_error, session.getSessionId());
                hintOrError(!isWarning, errors, NO_RULE_DATE, IssueType.NOTFOUND, stack, false, I18nConstants.BUNDLE_BUNDLE_ENTRY_NOTFOUND, ref, name);
              }            
            }
          }
        }
        return null;    
      }
    }
  }

  protected List<Element> getUrlMatches(Element element, String url, NodeStack stack) {
    List<Element> result = new ArrayList<>(); 
    if (element.isResource() && element.hasParentForValidator()) {
      Element bnd = getElementBundle(element);
      if (bnd != null) {
        // in this case, we look into the parent - if there is one - and if it's a bundle, we look at the entries (but not in them)
        for (Element be : bnd.getChildrenByName("entry")) {
          if (be.hasChild("resource")) {
            String t = be.getNamedChild("resource").fhirType()+"/"+be.getNamedChild("resource").getIdBase();
            if (url.equals(t)) {
              result.add(be.getNamedChild("resource"));
            } else {
              t = be.getNamedChildValue("fullUrl");
              if (url.equals(t)) {
                result.add(be.getNamedChild("resource"));
              }
            }
          }
        }
      }
    }
    return result;
  }

  protected List<Element> getFragmentMatches(Element element, String fragment, NodeStack stack) {
    List<Element> result = getFragmentMatches(element, fragment); 
    if (element.isResource() && element.hasParentForValidator()) {
      Element bnd = getElementBundle(element);
      if (bnd != null) {
        // in this case, we look into the parent - if there is one - and if it's a bundle, we look at the entries (but not in them)
        for (Element be : bnd.getChildrenByName("entry")) {
          if (be.hasChild("resource")) {
            String id = be.getNamedChild("resource").getIdBase();
            if (fragment.equals(id)) {
              result.add(be.getNamedChild("resource"));
            }
          }
        }
      }
    }
    return result;
  }

  protected int countFragmentMatches(Element element, String fragment, NodeStack stack) {
    int count = countFragmentMatches(element, fragment); 
    if (count == 0 && element.isResource() && element.hasParentForValidator()) {
      Element bnd = getElementBundle(element);
      if (bnd != null) {
        // in this case, we look into the parent - if there is one - and if it's a bundle, we look at the entries (but not in them)
        for (Element be : bnd.getChildrenByName("entry")) {
          if (be.hasChild("resource")) {
            String id = be.getNamedChild("resource").getIdBase();
            if (fragment.equals(id)) {
              count++;
            }
          }
        }
      }
    }
    return count;
  }
  
  private Element getElementBundle(Element element) {
    Element p = element.getParentForValidator();
    if (p != null) {
      Element b = p.getParentForValidator();
      if (b != null && b.fhirType().equals("Bundle")) {
        return b;
      }
    }
    return null;
  }

  protected List<Element> getFragmentMatches(Element element, String fragment) {
    List<Element> result = new ArrayList<>();
    if (fragment.equals(element.getIdBase())) {
      result.add(element);
    }
    if (element.hasChildren()) {
      for (Element child : element.getChildren()) {
        result.addAll(getFragmentMatches(child, fragment));
      }
    }
    return result;
  }

  protected int countFragmentMatches(Element element, String fragment) {
    int count = 0;
    if (fragment.equals(element.getIdBase())) {
      count++;
    }
    if (element.getXhtml() != null) {
      count = count + countFragmentMatches(element.getXhtml(), fragment);
    }
    if (element.hasChildren()) {
      for (Element child : element.getChildren()) {
        count = count + countFragmentMatches(child, fragment);
      }
    }
    return count;
  }

  private int countFragmentMatches(XhtmlNode node, String fragment) {
    int count = 0;
    if (fragment.equals(node.getAttribute("id"))) {
      count++;
    }
    if (node.hasChildren()) {
      for (XhtmlNode child : node.getChildNodes()) {
        count = count + countFragmentMatches(child, fragment);
      }
    }
    return count;
  }

  protected String extractResourceType(String ref) {
    String[] p = ref.split("\\/");
    return p[p.length -2];
  }

  protected IndexedElement getFromBundle(Element bundle, String ref, String fullUrl, List<ValidationMessage> errors, String path, String type, boolean isTransaction, BooleanHolder bh) {
    String targetUrl;
    String version = "";
    String resourceType = null;
    if (ref.startsWith("http:") || ref.startsWith("urn:") || Utilities.isAbsoluteUrl(ref)) {
      // We've got an absolute reference, no need to calculate
      if (ref.contains("/_history/")) {
        targetUrl = ref.substring(0, ref.indexOf("/_history/") - 1);
        version = ref.substring(ref.indexOf("/_history/") + 10);
      } else
        targetUrl = ref;

    } else if (fullUrl == null) {
      //This isn't a problem for signatures - if it's a signature, we won't have a resolution for a relative reference.  For anything else, this is an error
      // but this rule doesn't apply for batches or transactions
      rule(errors, NO_RULE_DATE, IssueType.REQUIRED, -1, -1, path, Utilities.existsInList(type, "batch-response", "transaction-response") || path.startsWith("Bundle.signature"), I18nConstants.BUNDLE_BUNDLE_FULLURL_MISSING);
      return null;

    } else if (StringUtils.countMatches(ref, '/') != 1 && StringUtils.countMatches(ref, '/') != 3) {
      if (isTransaction) {
        rule(errors, NO_RULE_DATE, IssueType.INVALID, -1, -1, path, isSearchUrl(context, ref), I18nConstants.REFERENCE_REF_FORMAT1, ref);
      } else {
        rule(errors, NO_RULE_DATE, IssueType.INVALID, -1, -1, path, false, I18nConstants.REFERENCE_REF_FORMAT2, ref);
      }
      return null;

    } else {
      String base = "";
      if (fullUrl.startsWith("urn")) {
        String[] parts = fullUrl.split("\\:");
        for (int i = 0; i < parts.length - 1; i++) {
          base = base + parts[i] + ":";
        }
      } else {
        String[] parts;
        parts = fullUrl.split("/");
        for (int i = 0; i < parts.length - 2; i++) {
          base = base + parts[i] + "/";
        }
      }

      String id = null;
      if (ref.contains("/_history/")) {
        version = ref.substring(ref.indexOf("/_history/") + 10);
        String[] refBaseParts = ref.substring(0, ref.indexOf("/_history/")).split("/");
        resourceType = refBaseParts[0];
        id = refBaseParts[1];
        targetUrl = base + resourceType+"/"+ id;
      } else if (base.startsWith("urn")) {
        resourceType = ref.split("/")[0];
        id = ref.split("/")[1];
        targetUrl = base + id;
      } else {
        id = ref;
        targetUrl = base + id;
      }
    }

    List<Element> entries = new ArrayList<Element>();
    bundle.getNamedChildren(ENTRY, entries);
    Element match = null;
    int matchIndex = -1;
    for (int i = 0; i < entries.size(); i++) {
      Element we = entries.get(i);
      if (targetUrl.equals(we.getChildValue(FULL_URL))) {
        Element r = we.getNamedChild(RESOURCE, false);
        if (version.isEmpty()) {
          rule(errors, NO_RULE_DATE, IssueType.FORBIDDEN, -1, -1, path, match == null, I18nConstants.BUNDLE_BUNDLE_MULTIPLEMATCHES, ref);
          match = r;
          matchIndex = i;
        } else {
          try {
            if (version.equals(r.getChildren(META).get(0).getChildValue("versionId"))) {
              rule(errors, NO_RULE_DATE, IssueType.FORBIDDEN, -1, -1, path, match == null, I18nConstants.BUNDLE_BUNDLE_MULTIPLEMATCHES, ref);
              match = r;
              matchIndex = i;
            }
          } catch (Exception e) {
            warning(errors, NO_RULE_DATE, IssueType.REQUIRED, -1, -1, path, r.getChildren(META).size() == 1 && r.getChildren(META).get(0).getChildValue("versionId") != null, I18nConstants.BUNDLE_BUNDLE_FULLURL_NEEDVERSION, targetUrl);
            // If one of these things is null
          }
        }
      }
    }

    if (match != null && resourceType != null)
      bh.see(rule(errors, NO_RULE_DATE, IssueType.REQUIRED, -1, -1, path, match.getType().equals(resourceType), I18nConstants.REFERENCE_REF_RESOURCETYPE, ref, match.getType()));
    if (match == null) {
      warning(errors, NO_RULE_DATE, IssueType.REQUIRED, -1, -1, path, !ref.startsWith("urn"), I18nConstants.BUNDLE_BUNDLE_NOT_LOCAL, ref);
      if (!Utilities.isAbsoluteUrl(ref)) {
        String[] p = ref.split("\\/");
        List<Element> ml = new ArrayList<>();
        if (p.length >= 2 && context.getResourceNamesAsSet().contains(p[0]) && Utilities.isValidId(p[1])) {
          for (int i = 0; i < entries.size(); i++) {
            Element we = entries.get(i);
            Element r = we.getNamedChild(RESOURCE, false);
            if (r != null && p[0].equals(r.fhirType()) && p[1].equals(r.getNamedChildValue("id", false)) ) {
              ml.add(we);
            }
          }          
        }
        if (ml.size() > 1) {
          warning(errors, NO_RULE_DATE, IssueType.REQUIRED, -1, -1, path, false, I18nConstants.BUNDLE_POSSSIBLE_MATCHES, ref, targetUrl);          
        }
        for (Element e : ml) {
          String fu = e.getChildValue(FULL_URL);
          int i = entries.indexOf(e);
          if (fu == null) {
            warning(errors, NO_RULE_DATE, IssueType.REQUIRED, -1, -1, path, false, I18nConstants.BUNDLE_BUNDLE_POSSIBLE_MATCH_NO_FU, i, ref, targetUrl);
          } else {
            warning(errors, NO_RULE_DATE, IssueType.REQUIRED, -1, -1, path, false, I18nConstants.BUNDLE_BUNDLE_POSSIBLE_MATCH_WRONG_FU, i, ref, fu, targetUrl);            
          }
        }
      }
    }
    return match == null ? null : new IndexedElement(matchIndex, match, entries.get(matchIndex));
  }

  /**
   * Determines whether a string is a valid search URL. A valid search URL takes
   * the forms:
   * [resourceType]?[paramName]=[paramValue]
   * [resourceType]?[paramName]=[paramValue]&[paramName]=[paramValue]
   * [resourceType]?[paramName]=[paramValue]&[paramName]=[paramValue]&....
   */
  public static boolean isSearchUrl(IWorkerContext context, String ref) {
    if (Utilities.noString(ref)) {
      return false;
    }

    int questionMarkIndex = ref.indexOf("?");
    if (questionMarkIndex == -1) {
      return false;
    }

    String resourceType = ref.substring(0, questionMarkIndex);
    String query = ref.substring(questionMarkIndex + 1);
    if (!context.getResourceNamesAsSet().contains(resourceType)) {
      return false;
    } else {
      return SEARCH_URL_PARAMS.matcher(query).matches();
    }
  }

  public Map<String, ValidationControl> getValidationControl() {
    return validationControl;
  }

  public XVerExtensionStatus xverStatus(String url) {
    return xverManager.status(url);
  }

  public boolean isXverUrl(String url) {
    return xverManager.matchingUrl(url);    
  }
  
  public StructureDefinition xverDefn(String url) {
    return xverManager.makeDefinition(url);
  }
  
  public String xverVersion(String url) {
    return xverManager.getVersion(url);
  }

  public String xverElementId(String url) {
    return xverManager.getElementId(url);
  }

  public StructureDefinition getXverExt(StructureDefinition profile, List<ValidationMessage> errors, String url) {
    if (isXverUrl(url)) {
      switch (xverStatus(url)) {
        case BadVersion:
          rule(errors, NO_RULE_DATE, IssueType.BUSINESSRULE, profile.getId(), false, I18nConstants.EXTENSION_EXT_VERSION_INVALID, url, xverVersion(url));
          return null;
        case Unknown:
          rule(errors, NO_RULE_DATE, IssueType.BUSINESSRULE, profile.getId(), false, I18nConstants.EXTENSION_EXT_VERSION_INVALIDID, url, xverElementId(url));
          return null;
        case Invalid:
          rule(errors, NO_RULE_DATE, IssueType.BUSINESSRULE, profile.getId(), false, I18nConstants.EXTENSION_EXT_VERSION_NOCHANGE, url, xverElementId(url));
          return null;
        case Valid:
          StructureDefinition defn = xverDefn(url);
          new ContextUtilities(context).generateSnapshot(defn);
          context.cacheResource(defn);
          return defn;
        default:
          rule(errors, NO_RULE_DATE, IssueType.INVALID, profile.getId(), false, I18nConstants.EXTENSION_EXT_VERSION_INTERNAL, url);
          return null;
      }
    } else {
      return null;      
    }
  }
  
  public StructureDefinition getXverExt(List<ValidationMessage> errors, String path, Element element, String url) {
    if (isXverUrl(url)) {
      switch (xverStatus(url)) {
      case BadVersion:
        rule(errors, NO_RULE_DATE, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, I18nConstants.EXTENSION_EXT_VERSION_INVALID, url, xverVersion(url));
        break;
      case Unknown:
        rule(errors, NO_RULE_DATE, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, I18nConstants.EXTENSION_EXT_VERSION_INVALIDID, url, xverElementId(url));
        break;
      case Invalid:
        rule(errors, NO_RULE_DATE, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, I18nConstants.EXTENSION_EXT_VERSION_NOCHANGE, url, xverElementId(url));
        break;
      case Valid:
        StructureDefinition ex = xverDefn(url);
        new ContextUtilities(context).generateSnapshot(ex);
        context.cacheResource(ex);
        return ex;
      default:
        rule(errors, NO_RULE_DATE, IssueType.INVALID, element.line(), element.col(), path + "[url='" + url + "']", false, I18nConstants.EXTENSION_EXT_VERSION_INTERNAL, url);
        break;
      }
    }
    return null;
  }
  

  
  @Override
  public Resource loadContainedResource(List<ValidationMessage> errors, String path, Element resource, String id, Class<? extends Resource> class1) throws FHIRException {
    for (Element contained : resource.getChildren("contained")) {
      if (contained.getIdBase().equals(id)) {
        return loadFoundResource(errors, path, contained, class1);
      }
    }
    return null;
  }
  
  protected Resource loadFoundResource(List<ValidationMessage> errors, String path, Element resource, Class<? extends Resource> class1) throws FHIRException {
    try {
      FhirPublication v = FhirPublication.fromCode(context.getVersion());
      ByteArrayOutputStream bs = new ByteArrayOutputStream();
      new JsonParser(context).compose(resource, bs, OutputStyle.NORMAL, resource.getIdBase());
      byte[] json = bs.toByteArray();
      Resource r5 = null;
      switch (v) {
      case DSTU1:
        rule(errors, NO_RULE_DATE, IssueType.INVALID, resource.line(), resource.col(), path, false, I18nConstants.UNSUPPORTED_VERSION_R1, resource.getIdBase());
        return null; // this can't happen
      case DSTU2:
        org.hl7.fhir.dstu2.model.Resource r2 = new org.hl7.fhir.dstu2.formats.JsonParser().parse(json);
        r5 = VersionConvertorFactory_10_50.convertResource(r2);
        break;
      case DSTU2016May:
        org.hl7.fhir.dstu2016may.model.Resource r2a = new org.hl7.fhir.dstu2016may.formats.JsonParser().parse(json);
        r5 = VersionConvertorFactory_14_50.convertResource(r2a);
        break;
      case STU3:
        org.hl7.fhir.dstu3.model.Resource r3 = new org.hl7.fhir.dstu3.formats.JsonParser().parse(json);
        r5 = VersionConvertorFactory_30_50.convertResource(r3);
        break;
      case R4:
        org.hl7.fhir.r4.model.Resource r4 = new org.hl7.fhir.r4.formats.JsonParser().parse(json);
        r5 = VersionConvertorFactory_40_50.convertResource(r4);
        break;
      case R5:
        r5 = new org.hl7.fhir.r5.formats.JsonParser().parse(json);
        break;
      default:
        return null; // this can't happen
      }
      if (class1.isInstance(r5))
        return (Resource) r5;
      else {
        rule(errors, NO_RULE_DATE, IssueType.INVALID, resource.line(), resource.col(), path, false, I18nConstants.REFERENCE_REF_WRONGTARGET_LOAD, resource.getIdBase(), class1.toString(), r5.fhirType());
        return null;
      }

    } catch (IOException e) {
      throw new FHIRException(e);
    }
  }

  protected boolean isHL7(Element cr) {
    String url = cr.getChildValue("url");
    return url != null && url.contains("hl7");
  }

  protected boolean isHL7Org(Element cr) {
    String url = cr.getChildValue("url");
    return url != null && url.startsWith("http://hl7.org/fhir/");
  }

  protected boolean isHL7Core(Element cr) {
    String url = cr.getChildValue("url");
    if ("CodeSystem".equals(cr.fhirType())) {
      if (("http://hl7.org/fhir/"+cr.getIdBase()).equals(url)) {
        return true;
      }
    }
    return url != null && url.startsWith("http://hl7.org/fhir/"+cr.fhirType());
  }

  protected boolean isExampleUrl(String url) {
    return 
        Utilities.containsInList(url, "example.org/", "acme.com/", "acme.org/", "example.com/", "example.net/") ||
        Utilities.endsWithInList(url, "example.org", "acme.com", "acme.org", "example.com", "example.net") ||
        url.startsWith("urn:oid:1.3.6.1.4.1.32473.") || url.startsWith("urn:oid:2.999.");    
  }
  
  protected boolean checkDefinitionStatus(List<ValidationMessage> errors, Element element, String path, StructureDefinition ex, CanonicalResource source, String type) {
    boolean ok = true;
    String vurl = ex.getVersionedUrl();

    StandardsStatus standardsStatus = ExtensionUtilities.getStandardsStatus(ex);
    
    if (standardsStatus == StandardsStatus.DEPRECATED) {
      if (!statusWarnings.contains(vurl+":DEPRECATED")) {
        Extension ext = ex.getExtensionByUrl(ExtensionDefinitions.EXT_STANDARDS_STATUS);
        ext = ext == null || !ext.hasValue() ? null : ext.getValue().getExtensionByUrl(ExtensionDefinitions.EXT_STANDARDS_STATUS_REASON);
        String note = ext == null || !ext.hasValue() ? null : MarkDownProcessor.markdownToPlainText(ext.getValue().primitiveValue());

        statusWarnings.add(vurl+":DEPRECATED");
        hint(errors, "2023-08-10", IssueType.BUSINESSRULE, element.line(), element.col(), path, false, 
            Utilities.noString(note) ? I18nConstants.MSG_DEPENDS_ON_DEPRECATED : I18nConstants.MSG_DEPENDS_ON_DEPRECATED_NOTE, type, vurl, note);
      }
    } else if (standardsStatus == StandardsStatus.WITHDRAWN) {
      if (!statusWarnings.contains(vurl+":WITHDRAWN")) {  
        statusWarnings.add(vurl+":WITHDRAWN");
        hint(errors, "2023-08-10", IssueType.BUSINESSRULE, element.line(), element.col(), path, false, I18nConstants.MSG_DEPENDS_ON_WITHDRAWN, type, vurl);
      }
    } else if (ex.getStatus() == PublicationStatus.RETIRED) {
      if (!statusWarnings.contains(vurl+":RETIRED")) {  
        statusWarnings.add(vurl+":RETIRED");
        hint(errors, "2023-08-10", IssueType.BUSINESSRULE, element.line(), element.col(), path, false, I18nConstants.MSG_DEPENDS_ON_RETIRED, type, vurl);
      }
    } else if (false && settings.isWarnOnDraftOrExperimental() && source != null) {
      // for now, this is disabled; these warnings are just everywhere, and it's an intractible problem. 
      // working this through QA in IG publisher
      if (ex.getExperimental() && !source.getExperimental()) {
        if (!statusWarnings.contains(vurl+":Experimental")) {  
          statusWarnings.add(vurl+":Experimental");
          hint(errors, "2023-08-10", IssueType.BUSINESSRULE, element.line(), element.col(), path, false, I18nConstants.MSG_DEPENDS_ON_EXPERIMENTAL, type, vurl);
        }
      } else if (ex.getStatus() == PublicationStatus.DRAFT && source.getStatus() != PublicationStatus.DRAFT) {
        if (!statusWarnings.contains(vurl+":Draft")) {  
          statusWarnings.add(vurl+":Draft");
          hint(errors, "2023-08-10", IssueType.BUSINESSRULE, element.line(), element.col(), path, false, I18nConstants.MSG_DEPENDS_ON_DRAFT, type, vurl);
        }
      }
    }
    return ok;
  }

  
  protected boolean bpCheck(List<ValidationMessage> errors, IssueType invalid, int line, int col, String literalPath, boolean test, String message, Object... theMessageArguments) {
    if (settings.getBpWarnings() != null) {
      switch (settings.getBpWarnings()) {
        case Error:
          rule(errors, NO_RULE_DATE, invalid, line, col, literalPath, test, message, theMessageArguments);
          return test;
        case Warning:
          warning(errors, NO_RULE_DATE, invalid, line, col, literalPath, test, message, theMessageArguments);
          return true;
        case Hint:
          hint(errors, NO_RULE_DATE, invalid, line, col, literalPath, test, message, theMessageArguments);
          return true;
        default: // do nothing
          break;
      }
    }
    return true;
  }

  protected boolean hasUseContext(Coding use, Coding value) {
    for (UsageContext usage : settings.getUsageContexts())  {
      if (isContext(use, value, usage)) {
        return true;
      }
    }
    return false;
  }

  private boolean isContext(Coding use, Coding value, UsageContext usage) {
    return usage.getValue() instanceof Coding && context.subsumes(settings, usage.getCode(), use) && context.subsumes(settings, (Coding) usage.getValue(), value);
  }
  

  protected boolean isKnownUsage(UsageContext usage) {
    for (UsageContext t : settings.getUsageContexts()) {
      if (usagesMatch(usage, t)) {
        return true;
      }
    }
    return false;
  }

  private boolean usagesMatch(UsageContext usage, UsageContext t) {
    if (usage.hasCode() && t.hasCode() && usage.hasValue() && t.hasValue()) {
      if (usage.getCode().matches(t.getCode())) {
        if (usage.getValue().fhirType().equals(t.getValue().fhirType())) {
          switch (usage.getValue().fhirType()) {
          case "CodeableConcept": 
            for (Coding uc : usage.getValueCodeableConcept().getCoding()) {              
              for (Coding tc : t.getValueCodeableConcept().getCoding()) {
                if (uc.matches(tc)) {
                  return true;
                }
              }
            }
          case "Quantity":  
            return false; // for now
          case "Range": 
            return false; // for now
          case "Reference":
            return false; // for now
          }
        }
      }
    }
    return false;
  }

  public IValidationPolicyAdvisor getPolicyAdvisor() {
    return policyAdvisor;
  }

  public void setPolicyAdvisor(IValidationPolicyAdvisor advisor) {
    if (advisor == null) {
      throw new Error("Cannot set advisor to null");
    }
    this.policyAdvisor = advisor;
  }


  protected InstanceValidator validator() {
    if (this instanceof InstanceValidator) {
      return (InstanceValidator) this;
    } else {
      return (InstanceValidator) parent;
    }
  }

  public ValidatorSettings getSettings() {
    return settings;
  }

}