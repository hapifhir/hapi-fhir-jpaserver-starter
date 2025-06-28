package ch.ahdis.matchbox.engine.cli;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r5.context.ContextUtilities;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.context.Slf4JLoggingService;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.OperationOutcome;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureMap;
import org.hl7.fhir.r5.terminologies.CodeSystemUtilities;
import org.hl7.fhir.r5.terminologies.client.TerminologyClientManager.InternalLogEvent;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.utilities.FileUtilities;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.IgLoader;
import org.hl7.fhir.validation.ValidationEngine;
import org.hl7.fhir.validation.ValidationRecord;
import org.hl7.fhir.validation.ValidatorUtils;
import org.hl7.fhir.validation.instance.advisor.BasePolicyAdvisorForFullValidation;
import org.hl7.fhir.validation.service.model.ValidationContext;
import org.hl7.fhir.validation.service.model.FileInfo;
import org.hl7.fhir.validation.service.model.ValidationOutcome;
import org.hl7.fhir.validation.service.model.ValidationRequest;
import org.hl7.fhir.validation.service.model.ValidationResponse;
import org.hl7.fhir.validation.service.renderers.CSVRenderer;
import org.hl7.fhir.validation.service.renderers.DefaultRenderer;
import org.hl7.fhir.validation.service.renderers.ESLintCompactRenderer;
import org.hl7.fhir.validation.service.renderers.NativeRenderer;
import org.hl7.fhir.validation.service.renderers.ValidationOutputRenderer;
import org.hl7.fhir.validation.service.DisabledValidationPolicyAdvisor;
import org.hl7.fhir.validation.service.HTMLOutputGenerator;
import org.hl7.fhir.validation.service.PassiveExpiringSessionCache;
import org.hl7.fhir.validation.service.StandAloneValidatorFetcher;
import org.hl7.fhir.validation.service.utils.EngineMode;
import org.hl7.fhir.validation.service.utils.VersionSourceInformation;

import ch.ahdis.matchbox.engine.CdaMappingEngine;
import ch.ahdis.matchbox.engine.MatchboxEngine;
import ch.ahdis.matchbox.engine.ValidationPolicyAdvisor;
import lombok.extern.slf4j.Slf4j;

/**
 * A executable class 
 * 
 * adapted from https://github.com/hapifhir/org.hl7.fhir.core/blob/master/org.hl7.fhir.validation/
 *
 * @author Oliver Egger
 */
@Slf4j
public class MatchboxService {

  public static final String CURRENT_DEFAULT_VERSION = "4.0";
  public static final String CURRENT_DEFAULT_FULL_VERSION = "4.0.1";

  private final PassiveExpiringSessionCache sessionCache;

  public MatchboxService() {
    sessionCache = new PassiveExpiringSessionCache();
  }

  protected MatchboxService(final PassiveExpiringSessionCache cache) {
    this.sessionCache = cache;
  }

  public ValidationResponse validateSources(ValidationRequest request) throws Exception {
    if (request.getValidationContext().getSv() == null) {
      String sv = determineVersion(request.getValidationContext(), request.sessionId);
      request.getValidationContext().setSv(sv);
    }

    String definitions = VersionUtilities.packageForVersion(request.getValidationContext().getSv()) + "#" + VersionUtilities.getCurrentVersion(request.getValidationContext().getSv());

    String sessionId = initializeValidator(request.getValidationContext(), definitions, new TimeTracker(), request.sessionId);
    ValidationEngine validator = sessionCache.fetchSessionValidatorEngine(sessionId);

    if (request.getValidationContext().getProfiles().size() > 0) {
      System.out.println("  .. validate " + request.listSourceFiles() + " against " + request.getValidationContext().getProfiles().toString());
    } else {
      System.out.println("  .. validate " + request.listSourceFiles());
    }

    ValidationResponse response = new ValidationResponse().setSessionId(sessionId);

    for (FileInfo fp : request.getFilesToValidate()) {
      List<ValidationMessage> messages = new ArrayList<>();
      validator.validate(fp.getFileContent().getBytes(), Manager.FhirFormat.getFhirFormat(fp.getFileType()),
        request.getValidationContext().getProfiles(), messages);
      ValidationOutcome outcome = new ValidationOutcome().setFileInfo(fp);
      messages.forEach(outcome::addMessage);
      response.addOutcome(outcome);
    }
    System.out.println("  Max Memory: "+Runtime.getRuntime().maxMemory());
    return response;
  }

  public VersionSourceInformation scanForVersions(ValidationContext validationContext) throws Exception {
    VersionSourceInformation versions = new VersionSourceInformation();
    IgLoader igLoader = new IgLoader(
      new FilesystemPackageCacheManager.Builder().build(),
      new SimpleWorkerContext.SimpleWorkerContextBuilder().fromNothing(),
      null);
    for (String src : validationContext.getIgs()) {
      igLoader.scanForIgVersion(src, validationContext.isRecursive(), versions);
    }
    igLoader.scanForVersions(validationContext.getSources(), versions);
    return versions;
  }

  public void validateSources(ValidationContext validationContext, ValidationEngine validator) throws Exception {
    long start = System.currentTimeMillis();
    List<ValidationRecord> records = new ArrayList<>();
	  List<ValidatorUtils.SourceFile> refs = new ArrayList<>();
    Resource r = validator.validate(validationContext.getSources(), validationContext.getProfiles(), refs, records, null, true, 0,
												true);
    MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
    System.out.println("Done. " + validator.getContext().clock().report()+". Memory = "+Utilities.describeSize(mbean.getHeapMemoryUsage().getUsed()+mbean.getNonHeapMemoryUsage().getUsed()));
    System.out.println();

    PrintStream dst = null;
    if (validationContext.getOutput() == null) {
      dst = System.out;
    } else {
      dst = new PrintStream(new FileOutputStream(validationContext.getOutput()));
    }

    ValidationOutputRenderer renderer = makeValidationOutputRenderer(validationContext);
    renderer.setOutput(dst);
    renderer.setCrumbTrails(validator.isCrumbTrails());
    renderer.setShowMessageIds(validator.isShowMessageIds());

    int ec = 0;
    
    if (r instanceof Bundle) {
      if (renderer.handlesBundleDirectly()) {
        renderer.render((Bundle) r);
      } else {
        renderer.start(((Bundle) r).getEntry().size() > 1);
        for (Bundle.BundleEntryComponent e : ((Bundle) r).getEntry()) {
          OperationOutcome op = (OperationOutcome) e.getResource();
          ec = ec + countErrors(op); 
          renderer.render(op);
        }
        renderer.finish();
      }
    } else if (r == null) {
      ec = ec + 1;
      System.out.println("No output from validation - nothing to validate");
    } else {
      renderer.start(false);
      OperationOutcome op = (OperationOutcome) r;
      ec = countErrors(op);
      renderer.render((OperationOutcome) r);
      renderer.finish();
    }
    
    if (validationContext.getOutput() != null) {
      dst.close();
    }

    if (validationContext.getHtmlOutput() != null) {
      String html = new HTMLOutputGenerator(records).generate(System.currentTimeMillis() - start);
      FileUtilities.stringToFile(html, validationContext.getHtmlOutput());
      System.out.println("HTML Summary in " + validationContext.getHtmlOutput());
    }

    if (validationContext.isShowTerminologyRouting()) {
      System.out.println("");
      System.out.println("Terminology Routing Dump ---------------------------------------");
      if (validator.getContext().getTxClientManager().getInternalLog().isEmpty()) {
        System.out.println("(nothing happened)");            
      } else {
        for (InternalLogEvent log : validator.getContext().getTxClientManager().getInternalLog()) {
          System.out.println(log.getMessage()+" -> "+log.getServer()+" (for VS "+log.getVs()+" with systems '"+log.getSystems()+"', choices = '"+log.getChoices()+"')");
        }
      }
      validator.getContext().getTxClientManager().getInternalLog().clear();
    }    

    System.exit(ec > 0 ? 1 : 0);
  }

  private int countErrors(OperationOutcome oo) {
    int error = 0;
    for (OperationOutcome.OperationOutcomeIssueComponent issue : oo.getIssue()) {
      if (issue.getSeverity() == OperationOutcome.IssueSeverity.FATAL || issue.getSeverity() == OperationOutcome.IssueSeverity.ERROR)
        error++;
    }
    return error;    
  }

  private ValidationOutputRenderer makeValidationOutputRenderer(ValidationContext validationContext) {
    String style = validationContext.getOutputStyle();
    // adding to this list? 
    // Must document the option at https://confluence.hl7.org/display/FHIR/Using+the+FHIR+Validator#UsingtheFHIRValidator-ManagingOutput
    // if you're going to make a PR, document the link where the outputstyle is documented, along with a sentence that describes it, in the PR notes 
    if (Utilities.noString(style)) {
      if (validationContext.getOutput() == null) {
        return new DefaultRenderer();        
      } else if (validationContext.getOutput().endsWith(".json")) {
        return new NativeRenderer(FhirFormat.JSON);
      } else {
        return new NativeRenderer(FhirFormat.XML);
      }
    } else if (Utilities.existsInList(style, "eslint-compact")) {
      return new ESLintCompactRenderer();
    } else if (Utilities.existsInList(style, "csv")) {
      return new CSVRenderer();
    } else if (Utilities.existsInList(style, "xml")) {
      return new NativeRenderer(FhirFormat.XML);
    } else if (Utilities.existsInList(style, "json")) {
      return new NativeRenderer(FhirFormat.JSON);
    } else {
      System.out.println("Unknown output style '"+style+"'");
      return new DefaultRenderer();      
    }
  }

  public void convertSources(ValidationContext validationContext, ValidationEngine validator) throws Exception {

      if (!((validationContext.getOutput() == null) ^ (validationContext.getOutputSuffix() == null))) {
        throw new Exception("Convert requires one of {-output, -outputSuffix} parameter to be set");
      }

      List<String> sources = validationContext.getSources();
      if ((sources.size() == 1) && (validationContext.getOutput() != null)) {
        System.out.println(" ...convert");
        validator.convert(sources.get(0), validationContext.getOutput());
      } else {
        if (validationContext.getOutputSuffix() == null) {
          throw new Exception("Converting multiple/wildcard sources requires a -outputSuffix parameter to be set");
        }
        for (int i = 0; i < sources.size(); i++) {
            String output = sources.get(i) + "." + validationContext.getOutputSuffix();
            validator.convert(sources.get(i), output);
            System.out.println(" ...convert [" + i +  "] (" + sources.get(i) + " to " + output + ")");
        }
      }
  }

  public void evaluateFhirpath(ValidationContext validationContext, ValidationEngine validator) throws Exception {
    System.out.println(" ...evaluating " + validationContext.getFhirpath());
    System.out.println(validator.evaluateFhirPath(validationContext.getSources().get(0), validationContext.getFhirpath()));
  }

  public void generateSnapshot(ValidationContext validationContext, ValidationEngine validator) throws Exception {

      if (!((validationContext.getOutput() == null) ^ (validationContext.getOutputSuffix() == null))) {
        throw new Exception("Snapshot generation requires one of {-output, -outputSuffix} parameter to be set");
      }

      List<String> sources = validationContext.getSources();
      if ((sources.size() == 1) && (validationContext.getOutput() != null)) {
        StructureDefinition r = validator.snapshot(sources.get(0), validationContext.getSv());
        System.out.println(" ...generated snapshot successfully");
        validator.handleOutput(r, validationContext.getOutput(), validationContext.getSv());
      } else {
        if (validationContext.getOutputSuffix() == null) {
          throw new Exception("Snapshot generation for multiple/wildcard sources requires a -outputSuffix parameter to be set");
        }
        for (int i = 0; i < sources.size(); i++) {
          StructureDefinition r = validator.snapshot(sources.get(i), validationContext.getSv());
          String output = sources.get(i) + "." + validationContext.getOutputSuffix();
          validator.handleOutput(r, output, validationContext.getSv());
          System.out.println(" ...generated snapshot [" + i +  "] successfully (" + sources.get(i) + " to " + output + ")");
        }
      }

  }

  public void generateNarrative(ValidationContext validationContext, ValidationEngine validator) throws Exception {
    Resource r = validator.generate(validationContext.getSources().get(0), validationContext.getSv());
    System.out.println(" ...generated narrative successfully");
    if (validationContext.getOutput() != null) {
      validator.handleOutput(r, validationContext.getOutput(), validationContext.getSv());
    }
  }

  public void transform(ValidationContext validationContext, ValidationEngine validator) throws Exception {
    if (validationContext.getSources().size() > 1)
      throw new Exception("Can only have one source when doing a transform (found " + validationContext.getSources() + ")");
    if (validationContext.getTxServer() == null)
      throw new Exception("Must provide a terminology server when doing a transform");
    if (validationContext.getMap() == null)
      throw new Exception("Must provide a map when doing a transform");
    try {
      ContextUtilities cu = new ContextUtilities(validator.getContext());
      List<StructureDefinition> structures =  cu.allStructures();
      for (StructureDefinition sd : structures) {
        if (!sd.hasSnapshot()) {
            cu.generateSnapshot(sd);
        }
      }
      validator.setMapLog(validationContext.getMapLog());
      org.hl7.fhir.r5.elementmodel.Element r = validator.transform(validationContext.getSources().get(0), validationContext.getMap());
      System.out.println(" ...success");
      if (validationContext.getOutput() != null) {
        FileOutputStream s = new FileOutputStream(validationContext.getOutput());
        if (validationContext.getOutput() != null && validationContext.getOutput().endsWith(".json"))
          new org.hl7.fhir.r5.elementmodel.JsonParser(validator.getContext()).compose(r, s, IParser.OutputStyle.PRETTY, null);
        else
          new org.hl7.fhir.r5.elementmodel.XmlParser(validator.getContext()).compose(r, s, IParser.OutputStyle.PRETTY, null);
        s.close();
      }
    } catch (Exception e) {
      System.out.println(" ...Failure: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void compile(ValidationContext validationContext, ValidationEngine validator) throws Exception {
    if (validationContext.getSources().size() > 0)
      throw new Exception("Cannot specify sources when compling transform (found " + validationContext.getSources() + ")");
    if (validationContext.getMap() == null)
      throw new Exception("Must provide a map when compiling a transform");
    if (validationContext.getOutput() == null)
      throw new Exception("Must provide an output name when compiling a transform");
    try {
      ContextUtilities cu = new ContextUtilities(validator.getContext());
      List<StructureDefinition> structures = cu.allStructures();
      for (StructureDefinition sd : structures) {
        if (!sd.hasSnapshot()) {
            cu.generateSnapshot(sd);
        }
      }
      validator.setMapLog(validationContext.getMapLog());
      StructureMap map = validator.compile(validationContext.getMap());
      if (map == null)
        throw new Exception("Unable to locate map " + validationContext.getMap());
      validator.handleOutput(map, validationContext.getOutput(), validator.getVersion());
      System.out.println(" ...success");
    } catch (Exception e) {
      System.out.println(" ...Failure: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public void transformVersion(ValidationContext validationContext, ValidationEngine validator) throws Exception {
    if (validationContext.getSources().size() > 1) {
      throw new Exception("Can only have one source when converting versions (found " + validationContext.getSources() + ")");
    }
    if (validationContext.getTargetVer() == null) {
      throw new Exception("Must provide a map when converting versions");
    }
    if (validationContext.getOutput() == null) {
      throw new Exception("Must nominate an output when converting versions");
    }
    try {
      if (validationContext.getMapLog() != null) {
        validator.setMapLog(validationContext.getMapLog());
      }
      byte[] r = validator.transformVersion(validationContext.getSources().get(0), validationContext.getTargetVer(), validationContext.getOutput().endsWith(".json") ? Manager.FhirFormat.JSON : Manager.FhirFormat.XML, validationContext.getCanDoNative());
      System.out.println(" ...success");
      FileUtilities.bytesToFile(r, validationContext.getOutput());
    } catch (Exception e) {
      System.out.println(" ...Failure: " + e.getMessage());
      e.printStackTrace();
    }
  }

  public MatchboxEngine initializeValidator(ValidationContext validationContext, String definitions, TimeTracker tt) throws Exception {
    return (MatchboxEngine) sessionCache.fetchSessionValidatorEngine(initializeValidator(validationContext, definitions, tt, null));
  }

  public String initializeValidator(ValidationContext validationContext, String definitions, TimeTracker tt, String sessionId) throws Exception {
    tt.milestone();

	 // getSessionIds() does the same thing as sessionCache.removeExpiredSessions() (protected access)
	 sessionCache.getSessionIds();

    if (!sessionCache.sessionExists(sessionId)) {
      if (sessionId != null) {
        System.out.println("No such cached session exists for session id " + sessionId + ", re-instantiating validator.");
      }
      System.out.println("  Initializing CdaMappingEngine for FHIR Version " + validationContext.getSv());
      
      CdaMappingEngine validator = new CdaMappingEngine.CdaMappingEngineBuilder().getCdaEngineR4();
      sessionId = sessionCache.cacheSession(validator);

      validator.setDebug(validationContext.isDoDebug());
      validator.getContext().setLogger(new Slf4JLoggingService(log));
      for (String src : validationContext.getIgs()) {
        validator.getIgLoader().loadIg(validator.getIgs(), validator.getBinaries(), src, validationContext.isRecursive());
      }
      validator.setQuestionnaireMode(validationContext.getQuestionnaireMode());
      validator.setLevel(validationContext.getLevel());
      validator.setDoNative(validationContext.isDoNative());
      validator.setHintAboutNonMustSupport(validationContext.isHintAboutNonMustSupport());
      for (String s : validationContext.getExtensions()) {
        if ("any".equals(s)) {
          validator.setAnyExtensionsAllowed(true);
        } else {          
          validator.getExtensionDomains().add(s);
        }
      }
      validator.setLanguage(validationContext.getLang());
      validator.setLocale(validationContext.getLocale());
      validator.setSnomedExtension(validationContext.getSnomedCTCode());
      validator.setDisplayWarnings(validationContext.isDisplayWarnings());
      validator.setAssumeValidRestReferences(validationContext.isAssumeValidRestReferences());
      validator.setShowMessagesFromReferences(validationContext.isShowMessagesFromReferences());
      validator.setDoImplicitFHIRPathStringConversion(validationContext.isDoImplicitFHIRPathStringConversion());
      validator.setHtmlInMarkdownCheck(validationContext.getHtmlInMarkdownCheck());
      validator.setNoExtensibleBindingMessages(validationContext.isNoExtensibleBindingMessages());
      validator.setNoUnicodeBiDiControlChars(validationContext.isNoUnicodeBiDiControlChars());
      validator.setNoInvariantChecks(validationContext.isNoInvariants());
      validator.setWantInvariantInMessage(validationContext.isWantInvariantsInMessages());
      validator.setSecurityChecks(validationContext.isSecurityChecks());
      validator.setCrumbTrails(validationContext.isCrumbTrails());
      validator.setForPublication(validationContext.isForPublication());
      validator.setShowTimes(validationContext.isShowTimes());
      validator.setAllowExampleUrls(validationContext.isAllowExampleUrls());
      validator.setR5BundleRelativeReferencePolicy(validationContext.getR5BundleRelativeReferencePolicy());
      ReferenceValidationPolicy refpol = ReferenceValidationPolicy.CHECK_VALID;
      if (!validationContext.isDisableDefaultResourceFetcher()) {
          StandAloneValidatorFetcher fetcher = new StandAloneValidatorFetcher(validator.getPcm(), validator.getContext(), validator);
          validator.setFetcher(fetcher);
          validator.getContext().setLocator(fetcher);
          validator.setPolicyAdvisor(fetcher);
          if (validationContext.isCheckReferences()) {
            fetcher.setReferencePolicy(ReferenceValidationPolicy.CHECK_VALID);
          } else {
            fetcher.setReferencePolicy(ReferenceValidationPolicy.IGNORE);
          }
          fetcher.setResolutionContext(validationContext.getResolutionContext());
        } else {
          DisabledValidationPolicyAdvisor fetcher = new DisabledValidationPolicyAdvisor();
          validator.setPolicyAdvisor(fetcher);
          refpol = ReferenceValidationPolicy.CHECK_TYPE_IF_EXISTS;
      }
      validator.getPolicyAdvisor().setPolicyAdvisor(new ValidationPolicyAdvisor(validator.getPolicyAdvisor() == null ? refpol : validator.getPolicyAdvisor().getReferencePolicy()));
      validator.getBundleValidationRules().addAll(validationContext.getBundleValidationRules());
      validator.setJurisdiction(CodeSystemUtilities.readCoding(validationContext.getJurisdiction()));
      
//      TerminologyCache.setNoCaching(validationContext.isNoInternalCaching());
      validator.prepare(); // generate any missing snapshots
      System.out.println(" go (" + tt.milestone() + ")");
    } else {
      System.out.println("Cached session exists for session id " + sessionId + ", returning stored validator session id.");
    }
    return sessionId;
  }


  

  public String determineVersion(ValidationContext validationContext) throws Exception {
    return determineVersion(validationContext, null);
  }

  public String determineVersion(ValidationContext validationContext, String sessionId) throws Exception {
    if (validationContext.getMode() != EngineMode.VALIDATION) {
      return "current";
    }
    System.out.println("Scanning for versions (no -version parameter):");
    VersionSourceInformation versions = scanForVersions(validationContext);
    for (String s : versions.getReport()) {
      if (!s.equals("(nothing found)")) {
        System.out.println("  " + s);
      }
    }
    if (versions.isEmpty()) {
      System.out.println("  No Version Info found: Using Default version '" + CURRENT_DEFAULT_VERSION + "'");
      return CURRENT_DEFAULT_FULL_VERSION;
    }
    if (versions.size() == 1) {
      System.out.println("-> use version " + versions.version());
      return versions.version();
    }
    throw new Exception("-> Multiple versions found. Specify a particular version using the -version parameter");
  }

}
