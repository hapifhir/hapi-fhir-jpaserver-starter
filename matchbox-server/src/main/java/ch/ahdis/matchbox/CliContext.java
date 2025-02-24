package ch.ahdis.matchbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.hl7.fhir.r5.model.*;
import org.hl7.fhir.r5.terminologies.JurisdictionUtilities;
import org.hl7.fhir.validation.cli.model.HtmlInMarkdownCheck;
import org.hl7.fhir.validation.cli.utils.EngineMode;
import org.hl7.fhir.validation.cli.utils.QuestionnaireMode;
import org.hl7.fhir.validation.cli.utils.ValidationLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.fasterxml.jackson.annotation.JsonProperty;

import static ch.ahdis.matchbox.util.MatchboxServerUtils.addExtension;

/**
 * A POJO for storing the flags/values for the CLI validator.
 * Needed to copy the class because the setters with CliContext as return type
 * are not accessible via reflection.
 * In addition we have parameters from the CliContext which do not make sense to
 * expose for the Web APi
 */
@Component
public class CliContext {

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CliContext.class);

  @JsonProperty("doNative")
  private boolean doNative = false;

  @JsonProperty("extensions")
  public List<String> getExtensions() {
    return extensions;
  }

  @JsonProperty("extensions")
  public CliContext setExtensions(List<String> extensions) {
    this.extensions = extensions;
    return this;
  }

  @JsonProperty("hintAboutNonMustSupport")
  private boolean hintAboutNonMustSupport = false;
  @JsonProperty("recursive")
  private boolean recursive = false;
  @JsonProperty("showMessagesFromReferences")
  private boolean showMessagesFromReferences = false;
  @JsonProperty("doDebug")
  private boolean doDebug = false;
  @JsonProperty("assumeValidRestReferences")
  private boolean assumeValidRestReferences = false;
  @JsonProperty("canDoNative")
  private boolean canDoNative = false;
  // @JsonProperty("noInternalCaching")
  // private boolean noInternalCaching = false; // internal, for when debugging
  // terminology validation
  @JsonProperty("noExtensibleBindingMessages")
  private boolean noExtensibleBindingMessages = false;
  @JsonProperty("noUnicodeBiDiControlChars")
  private boolean noUnicodeBiDiControlChars = false;
  @JsonProperty("noInvariants")
  private boolean noInvariants = false;
  @JsonProperty("displayIssuesAreWarnings")
  private boolean displayIssuesAreWarnings = true;
  @JsonProperty("wantInvariantsInMessages")
  private boolean wantInvariantsInMessages = false;
  @JsonProperty("doImplicitFHIRPathStringConversion")
  private boolean doImplicitFHIRPathStringConversion = false;
  @JsonProperty("htmlInMarkdownCheck")
  private HtmlInMarkdownCheck htmlInMarkdownCheck = HtmlInMarkdownCheck.WARNING;

  // @JsonProperty("map")
  // private String map = null;
  // @JsonProperty("output")
  // private String output = null;
  // @JsonProperty("outputSuffix")
  // private String outputSuffix;
  // @JsonProperty("htmlOutput")
  // private String htmlOutput = null;
  @JsonProperty("txServer")
  private String txServer = null;

  @JsonProperty("txServerCache")
  private boolean txServerCache = true;

  @JsonProperty("txLog")
  private String txLog = null;

  @JsonProperty("txUseEcosystem")
  private boolean txUseEcosystem = true;

  // @JsonProperty("sv")
  // private String sv = null;
  // @JsonProperty("mapLog")
  // private String mapLog = null;
  @JsonProperty("lang")
  private String lang = null;
  // @JsonProperty("fhirpath")
  // private String fhirpath = null;
  @JsonProperty("snomedCT")
  private String snomedCT = null;
  @JsonProperty("fhirVersion")
  private String fhirVersion = null;

  @JsonProperty("extensions")
  private List<String> extensions = new ArrayList<String>();
  // @JsonProperty("igs")
  // private List<String> igs = new ArrayList<String>();
  @JsonProperty("ig")
  private String ig = null;

  @JsonProperty("questionnaire")
  private QuestionnaireMode questionnaireMode = QuestionnaireMode.CHECK;
  @JsonProperty("level")
  private ValidationLevel level = ValidationLevel.HINTS;

  // @JsonProperty("profiles")
  // private List<String> profiles = new ArrayList<String>();
  // @JsonProperty("sources")
  // private List<String> sources = new ArrayList<String>();

  @JsonProperty("mode")
  private EngineMode mode = EngineMode.VALIDATION;

  @JsonProperty("securityChecks")
  private boolean securityChecks = false;

  @JsonProperty("crumbTrails")
  private boolean crumbTrails = false;

  @JsonProperty("forPublication")
  private boolean forPublication = false;

  @JsonProperty("showMessageIds")
  private boolean showMessageIds = false;

  @JsonProperty("showTerminologyRouting")
  private boolean showTerminologyRouting = false;

  @JsonProperty("clearTxCache")
  private boolean clearTxCache = false;

  @JsonProperty("allowExampleUrls")
  private boolean allowExampleUrls = true;

  // @JsonProperty("showTimes")
  // private boolean showTimes = false;

  @JsonProperty("locale")
  private String locale = Locale.ENGLISH.getLanguage();

  @JsonProperty("locations")
  private Map<String, String> locations = new HashMap<String, String>();

  // @JsonProperty("outputStyle")
  // private String outputStyle = null;

  @JsonProperty("jurisdiction")
  private String jurisdiction = JurisdictionUtilities.getJurisdictionFromLocale(Locale.getDefault().getCountry());

  @JsonProperty("check-ips-codes")
  private boolean checkIpsCodes = false;

  @JsonProperty("bundle")
  private String bundle = null;


  private String igsPreloaded[];

  public String[] getIgsPreloaded() {
    return igsPreloaded;
  }

  private boolean onlyOneEngine = false;

  public boolean getOnlyOneEngine() {
    return this.onlyOneEngine;
  }

  private boolean httpReadOnly = false;

  public boolean isHttpReadOnly() {
    return this.httpReadOnly;
  }

  private boolean xVersion = false;
  
  public boolean getXVersion() {
    return xVersion;
  }

  private String openaiAPIKey;

  public String getOpenaiAPIKey() {
    return openaiAPIKey;
  }
  
  @JsonProperty("check-references")
  private boolean checkReferences = false;

  // @JsonProperty("showTimes")
  // private boolean showTimes = false;

  @JsonProperty("resolution-context")
  private String resolutionContext = null;

  @JsonProperty("disableDefaultResourceFetcher")
  private boolean disableDefaultResourceFetcher = true;

  @JsonProperty("analyzeOutcomeWithAI")
  private boolean analyzeOutcomeWithAI = false;

  @Autowired
  public CliContext(Environment environment) {
    // get al list of all JsonProperty of cliContext with return values property
    // name and property type
    List<Field> cliContextProperties = getValidateEngineParameters();

    // check for each cliContextProperties if it is in the request parameter
    for (Field field : cliContextProperties) {
      String cliContextProperty = field.getName();
      String value = environment.getProperty("matchbox.fhir.context." + cliContextProperty);
      if (value != null && value.length() > 0) {
        try {
          if (field.getType() == boolean.class) {
            BeanUtils.setProperty(this, cliContextProperty, Boolean.parseBoolean(value));
          } else {
            BeanUtils.setProperty(this, cliContextProperty, value);
          }
        } catch (IllegalAccessException | InvocationTargetException e) {
          log.error("error setting property " + cliContextProperty + " to " + value);
        }
      }
    }
    // get properties array from the environment?
    this.igsPreloaded = environment.getProperty("matchbox.fhir.context.igsPreloaded", String[].class);
    this.onlyOneEngine = environment.getProperty("matchbox.fhir.context.onlyOneEngine", Boolean.class, false);
    this.httpReadOnly = environment.getProperty("matchbox.fhir.context.httpReadOnly", Boolean.class, false);
    this.extensions = Arrays.asList(environment.getProperty("matchbox.fhir.context.extensions", String[].class, new String[]{"any"}));
    this.xVersion = environment.getProperty("matchbox.fhir.context.xVersion", Boolean.class, false);
    this.openaiAPIKey = environment.getProperty("matchbox.fhir.context.openai_api_key", String.class);
    this.getOpenaiAPIKey();
  }

  public CliContext(CliContext other) {
    List<Field> cliContextProperties = getValidateEngineParameters();
    // check for each cliContextProperties if it is in the request parameter
    for (Field field : cliContextProperties) {
      try {
        String value = BeanUtils.getProperty(other, field.getName());
        if (value != null) {
          BeanUtils.setProperty(this, field.getName(), value);
        }
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        log.error("error setting property " + field.getName());
      }
    }
    this.igsPreloaded = other.igsPreloaded;
    this.onlyOneEngine = other.onlyOneEngine;
    this.httpReadOnly = other.httpReadOnly;
    this.extensions = other.extensions;
    this.xVersion = other.xVersion;
    this.openaiAPIKey = other.openaiAPIKey;
  }

  @JsonProperty("ig")
  public String getIg() {
    return ig;
  }

  @JsonProperty("ig")
  public void setIg(String ig) {
    this.ig = ig;
  }

  // @JsonProperty("igs")
  // public void setIgs(List<String> igs) {
  // this.igs = igs;
  // }

  // @JsonProperty("igs")
  // public List<String> getIgs() {
  // return igs;
  // }

  // @JsonProperty("igs")
  // public void setIgs(List<String> igs) {
  // this.igs = igs;
  // }

  // public CliContext addIg(String ig) {
  // if (this.igs == null) {
  // this.igs = new ArrayList<>();
  // }
  // this.igs.add(ig);
  // return this;
  // }

  @JsonProperty("questionnaire")
  public QuestionnaireMode getQuestionnaireMode() {
    return questionnaireMode;
  }

  @JsonProperty("questionnaire")
  public void setQuestionnaireMode(QuestionnaireMode questionnaireMode) {
    this.questionnaireMode = questionnaireMode;
  }

  @JsonProperty("level")
  public ValidationLevel getLevel() {
    return level;
  }

  @JsonProperty("level")
  public void setLevel(ValidationLevel level) {
    this.level = level;
  }

  @JsonProperty("txServer")
  public String getTxServer() {
    return txServer;
  }

  @JsonProperty("txServer")
  public void setTxServer(String txServer) {
    this.txServer = txServer;
  }

  @JsonProperty("txServerCache")
  public boolean getTxServerCache() {
    return txServerCache;
  }

  @JsonProperty("txServerCache")
  public void setTxServerCache(boolean txServerCache) {
    this.txServerCache = txServerCache;
  }

  @JsonProperty("txLog")
  public String getTxLog() {
    return txLog;
  }

  @JsonProperty("txLog")
  public void setTxLog(String txLog) {
    this.txLog = txLog;
  }

  @JsonProperty("txUseEcosystem")
  public void setTxUseEcosystem(boolean txUseEcosystem) {
    this.txUseEcosystem = txUseEcosystem;
  }

  @JsonProperty("txUseEcosystem")
  public boolean isTxUseEcosystem() {
    return txUseEcosystem;
  }

  @JsonProperty("doNative")
  public boolean isDoNative() {
    return doNative;
  }

  @JsonProperty("doNative")
  public void setDoNative(boolean doNative) {
    this.doNative = doNative;
  }

  @JsonProperty("hintAboutNonMustSupport")
  public boolean isHintAboutNonMustSupport() {
    return hintAboutNonMustSupport;
  }

  @JsonProperty("hintAboutNonMustSupport")
  public void setHintAboutNonMustSupport(boolean hintAboutNonMustSupport) {
    this.hintAboutNonMustSupport = hintAboutNonMustSupport;
  }

  @JsonProperty("recursive")
  public boolean isRecursive() {
    return recursive;
  }

  @JsonProperty("recursive")
  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  @JsonProperty("showMessagesFromReferences")
  public boolean isShowMessagesFromReferences() {
    return showMessagesFromReferences;
  }

  @JsonProperty("showMessagesFromReferences")
  public void setShowMessagesFromReferences(boolean showMessagesFromReferences) {
    this.showMessagesFromReferences = showMessagesFromReferences;
  }

  @JsonProperty("doImplicitFHIRPathStringConversion")
  public boolean isDoImplicitFHIRPathStringConversion() {
    return doImplicitFHIRPathStringConversion;
  }

  @JsonProperty("doImplicitFHIRPathStringConversion")
  public void setDoImplicitFHIRPathStringConversion(boolean doImplicitFHIRPathStringConversion) {
    this.doImplicitFHIRPathStringConversion = doImplicitFHIRPathStringConversion;
  }

  @JsonProperty("htmlInMarkdownCheck")
  public HtmlInMarkdownCheck getHtmlInMarkdownCheck() {
    return htmlInMarkdownCheck;
  }

  @JsonProperty("htmlInMarkdownCheck")
  public void setHtmlInMarkdownCheck(HtmlInMarkdownCheck htmlInMarkdownCheck) {
    this.htmlInMarkdownCheck = htmlInMarkdownCheck;
  }

  @JsonProperty("locale")
  public String getLocale() {
    return locale;
  }

  @JsonProperty("locale")
  public void setLocale(String locale) {
    this.locale = locale;
  }

  @JsonProperty("mode")
  public EngineMode getMode() {
    return mode;
  }

  @JsonProperty("mode")
  public void setMode(EngineMode mode) {
    this.mode = mode;
  }

  @JsonProperty("canDoNative")
  public boolean getCanDoNative() {
    return canDoNative;
  }

  @JsonProperty("canDoNative")
  public void setCanDoNative(boolean canDoNative) {
    this.canDoNative = canDoNative;
  }

  @JsonProperty("locations")
  public Map<String, String> getLocations() {
    return locations;
  }

  @JsonProperty("locations")
  public void setLocations(Map<String, String> locations) {
    this.locations = locations;
  }

  public CliContext addLocation(String profile, String location) {
    this.locations.put(profile, location);
    return this;
  }

  @JsonProperty("lang")
  public String getLang() {
    return lang;
  }

  @JsonProperty("lang")
  public void setLang(String lang) {
    this.lang = lang;
  }

  @JsonProperty("snomedCT")
  public String getSnomedCT() {
    if ("intl".equals(snomedCT))
      return "900000000000207008";
    if ("us".equals(snomedCT))
      return "731000124108";
    if ("uk".equals(snomedCT))
      return "999000041000000102";
    if ("au".equals(snomedCT))
      return "32506021000036107";
    if ("ca".equals(snomedCT))
      return "20611000087101";
    if ("nl".equals(snomedCT))
      return "11000146104";
    if ("se".equals(snomedCT))
      return "45991000052106";
    if ("es".equals(snomedCT))
      return "449081005";
    if ("dk".equals(snomedCT))
      return "554471000005108";
    if ("ch".equals(snomedCT))
      return "2011000195101";
    return snomedCT;
  }

  @JsonProperty("snomedCT")
  public void setSnomedCT(String snomedCT) {
    this.snomedCT = snomedCT;
  }

  @JsonProperty("fhirVersion")
  public String getFhirVersion() {
    return fhirVersion;
  }

  @JsonProperty("fhirVersion")
  public void setFhirVersion(String targetVer) {
    this.fhirVersion = targetVer;
  }

  @JsonProperty("doDebug")
  public boolean isDoDebug() {
    return doDebug;
  }

  @JsonProperty("doDebug")
  public void setDoDebug(boolean doDebug) {
    this.doDebug = doDebug;
  }

  @JsonProperty("assumeValidRestReferences")
  public boolean isAssumeValidRestReferences() {
    return assumeValidRestReferences;
  }

  @JsonProperty("assumeValidRestReferences")
  public void setAssumeValidRestReferences(boolean assumeValidRestReferences) {
    this.assumeValidRestReferences = assumeValidRestReferences;
  }

  // @JsonProperty("noInternalCaching")
  // public boolean isNoInternalCaching() {
  // return noInternalCaching;
  // }

  // @JsonProperty("noInternalCaching")
  // public void setNoInternalCaching(boolean noInternalCaching) {
  // this.noInternalCaching = noInternalCaching;
  // }

  @JsonProperty("noExtensibleBindingMessages")
  public boolean isNoExtensibleBindingMessages() {
    return noExtensibleBindingMessages;
  }

  @JsonProperty("noExtensibleBindingMessages")
  public void setNoExtensibleBindingMessages(boolean noExtensibleBindingMessages) {
    this.noExtensibleBindingMessages = noExtensibleBindingMessages;
  }

  @JsonProperty("noInvariants")
  public boolean isNoInvariants() {
    return noInvariants;
  }

  @JsonProperty("noInvariants")
  public void setNoInvariants(boolean noInvariants) {
    this.noInvariants = noInvariants;
  }

  @JsonProperty("displayIssuesAreWarnings")
  public boolean isDisplayIssuesAreWarnings() {
    return displayIssuesAreWarnings;
  }

  @JsonProperty("displayIssuesAreWarnings")
  public void setDisplayIssuesAreWarnings(boolean displayIssuesAreWarnings) {
    this.displayIssuesAreWarnings = displayIssuesAreWarnings;
  }

  @JsonProperty("wantInvariantsInMessages")
  public boolean isWantInvariantsInMessages() {
    return wantInvariantsInMessages;
  }

  @JsonProperty("wantInvariantsInMessages")
  public void setWantInvariantsInMessages(boolean wantInvariantsInMessages) {
    this.wantInvariantsInMessages = wantInvariantsInMessages;
  }

  @JsonProperty("securityChecks")
  public boolean isSecurityChecks() {
    return securityChecks;
  }

  @JsonProperty("securityChecks")
  public void setSecurityChecks(boolean securityChecks) {
    this.securityChecks = securityChecks;
  }

  public boolean isCrumbTrails() {
    return crumbTrails;
  }

  @JsonProperty("crumbTrails")
  public void setCrumbTrails(boolean crumbTrails) {
    this.crumbTrails = crumbTrails;
  }

  public boolean isForPublication() {
    return forPublication;
  }

  @JsonProperty("forPublication")
  public void setForPublication(boolean forPublication) {
    this.forPublication = forPublication;
  }

  public boolean isShowMessageIds() {
    return showMessageIds;
  }

  public void setShowMessageIds(boolean showMessageIds) {
    this.showMessageIds = showMessageIds;
  }

  public boolean isShowTerminologyRouting() {
    return showTerminologyRouting;
  }

  public void setShowTerminologyRouting(boolean showTerminologyRouting) {
    this.showTerminologyRouting = showTerminologyRouting;
  }

  public boolean isClearTxCache() {
    return clearTxCache;
  }

  public void setClearTxCache(boolean clearTxCache) {
    this.clearTxCache = clearTxCache;
  }

  public boolean isAllowExampleUrls() {
    return allowExampleUrls;
  }

  @JsonProperty("allowExampleUrls")
  public void setAllowExampleUrls(boolean allowExampleUrls) {
    this.allowExampleUrls = allowExampleUrls;
  }

  public boolean isNoUnicodeBiDiControlChars() {
    return noUnicodeBiDiControlChars;
  }

  @JsonProperty("noUnicodeBiDiControlChars")
  public void setNoUnicodeBiDiControlChars(boolean noUnicodeBiDiControlChars) {
    this.noUnicodeBiDiControlChars = noUnicodeBiDiControlChars;
  }

  public String getJurisdiction() {
    return jurisdiction;
  }

  @JsonProperty("jurisdiction")
  public void setJurisdiction(String jurisdiction) {
    this.jurisdiction = jurisdiction;
  }
  
  public boolean isCheckReferences() {
    return this.checkReferences;
  }

  @JsonProperty("check-references")
  public void setCheckReferences(boolean checkReferences) {
    this.checkReferences = checkReferences;
  }
  
  public String getResolutionContext() {
      return this.resolutionContext;
  }

  @JsonProperty("resolution-context")
  public void setResolutionContext(String resolutionContext) {
    this.resolutionContext = resolutionContext;
  }

  public boolean isDisableDefaultResourceFetcher() {
      return this.disableDefaultResourceFetcher;
    }

  @JsonProperty("disableDefaultResourceFetcher")
  public void setDisableDefaultResourceFetcher(boolean disableDefaultResourceFetcher) {
    this.disableDefaultResourceFetcher = disableDefaultResourceFetcher;
  }
  
  public boolean isCheckIpsCodes() {
    return this.checkIpsCodes;
  }

  @JsonProperty("check-ips-codes")
  public void setCheckIpsCodes(boolean checkIpsCodes) {
    this.checkIpsCodes = checkIpsCodes;
  }

  public String getBundle() {
    return this.bundle;
  }

  @JsonProperty("bundle")
  public void setBundle(String bundle) {
    this.bundle = bundle;
  }

  @JsonProperty("analyzeOutcomeWithAI")
  public boolean getAnalyzeOutcomeWithAI() {
    return analyzeOutcomeWithAI;
  }

  @JsonProperty("analyzeOutcomeWithAI")
  public void setAnalyzeOutcomeWithAI(boolean analyzeOutcomeWithAI) {
    this.analyzeOutcomeWithAI = analyzeOutcomeWithAI;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (!(o instanceof final CliContext that))
      return false;
    return doNative == that.doNative
        && hintAboutNonMustSupport == that.hintAboutNonMustSupport
        && recursive == that.recursive
        && showMessagesFromReferences == that.showMessagesFromReferences
        && doDebug == that.doDebug
        && assumeValidRestReferences == that.assumeValidRestReferences
        && canDoNative == that.canDoNative
        && noExtensibleBindingMessages == that.noExtensibleBindingMessages
        && noUnicodeBiDiControlChars == that.noUnicodeBiDiControlChars
        && noInvariants == that.noInvariants
        && displayIssuesAreWarnings == that.displayIssuesAreWarnings
        && wantInvariantsInMessages == that.wantInvariantsInMessages
        && doImplicitFHIRPathStringConversion == that.doImplicitFHIRPathStringConversion
        && securityChecks == that.securityChecks
        && crumbTrails == that.crumbTrails
        && forPublication == that.forPublication
        && showMessageIds == that.showMessageIds
        && showTerminologyRouting == that.showTerminologyRouting
        && clearTxCache == that.clearTxCache
        && allowExampleUrls == that.allowExampleUrls
        && onlyOneEngine == that.onlyOneEngine
        && xVersion == that.xVersion
        && httpReadOnly == that.httpReadOnly
        && htmlInMarkdownCheck == that.htmlInMarkdownCheck
        && Objects.equals(extensions, that.extensions)
        && Objects.equals(txServer, that.txServer)
        && txServerCache == that.txServerCache
        && Objects.equals(txLog, that.txLog)
        && txUseEcosystem == that.txUseEcosystem
        && Objects.equals(lang, that.lang)
        && Objects.equals(snomedCT, that.snomedCT)
        && Objects.equals(fhirVersion, that.fhirVersion)
        && Objects.equals(ig, that.ig)
        && questionnaireMode == that.questionnaireMode
        && level == that.level
        && mode == that.mode
        && Objects.equals(locale, that.locale)
        && Objects.equals(locations, that.locations)
        && Objects.equals(jurisdiction, that.jurisdiction)
        && Arrays.equals(igsPreloaded, that.igsPreloaded)
        && checkReferences == that.checkReferences
        && Objects.equals(resolutionContext, that.resolutionContext)
        && disableDefaultResourceFetcher == that.disableDefaultResourceFetcher
        && checkIpsCodes == that.checkIpsCodes
        && Objects.equals(bundle, that.bundle)
        && analyzeOutcomeWithAI == that.analyzeOutcomeWithAI;
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(doNative,
        extensions,
        hintAboutNonMustSupport,
        recursive,
        showMessagesFromReferences,
        doDebug,
        assumeValidRestReferences,
        canDoNative,
        noExtensibleBindingMessages,
        noUnicodeBiDiControlChars,
        noInvariants,
        displayIssuesAreWarnings,
        wantInvariantsInMessages,
        doImplicitFHIRPathStringConversion,
        securityChecks,
        crumbTrails,
        forPublication,
        showMessageIds,
        showTerminologyRouting,
        clearTxCache,
        httpReadOnly,
        allowExampleUrls,
        htmlInMarkdownCheck,
        txServer,
        txServerCache,
        txLog,
        txUseEcosystem,
        lang,
        snomedCT,
        fhirVersion,
        ig,
        questionnaireMode,
        level,
        mode,
        locale,
        locations,
        jurisdiction,
        onlyOneEngine,
        xVersion,
        checkReferences,
        resolutionContext,
        disableDefaultResourceFetcher,
        checkIpsCodes,
        bundle,
        analyzeOutcomeWithAI);
    result = 31 * result + Arrays.hashCode(igsPreloaded);
    return result;
  }

  @Override
  public String toString() {
    return "CliContext{" +
        "doNative=" + doNative +
        ", extensions=" + extensions +
        ", hintAboutNonMustSupport=" + hintAboutNonMustSupport +
        ", recursive=" + recursive +
        ", showMessagesFromReferences=" + showMessagesFromReferences +
        ", doDebug=" + doDebug +
        ", assumeValidRestReferences=" + assumeValidRestReferences +
        ", canDoNative=" + canDoNative +
        ", noExtensibleBindingMessages=" + noExtensibleBindingMessages +
        ", noUnicodeBiDiControlChars=" + noUnicodeBiDiControlChars +
        ", noInvariants=" + noInvariants +
        ", displayIssuesAreWarnings=" + displayIssuesAreWarnings +
        ", wantInvariantsInMessages=" + wantInvariantsInMessages +
        ", doImplicitFHIRPathStringConversion=" + doImplicitFHIRPathStringConversion +
        ", htmlInMarkdownCheck=" + htmlInMarkdownCheck +
        ", txServer='" + txServer + '\'' +
        ", txServerCache='" + txServerCache + '\'' +
        ", txLog='" + txLog + '\'' +
        ", txUseEcosystem=" + txUseEcosystem +
        ", lang='" + lang + '\'' +
        ", snomedCT='" + snomedCT + '\'' +
        ", fhirVersion='" + fhirVersion + '\'' +
        ", ig='" + ig + '\'' +
        ", questionnaireMode=" + questionnaireMode +
        ", level=" + level +
        ", mode=" + mode +
        ", securityChecks=" + securityChecks +
        ", crumbTrails=" + crumbTrails +
        ", forPublication=" + forPublication +
        ", showMessageIds=" + showMessageIds +
        ", showTerminologyRouting=" + showTerminologyRouting +
        ", clearTxCache=" + clearTxCache +
        ", allowExampleUrls=" + allowExampleUrls +
        ", locale='" + locale + '\'' +
        ", locations=" + locations +
        ", jurisdiction='" + jurisdiction + '\'' +
        ", igsPreloaded=" + Arrays.toString(igsPreloaded) +
        ", onlyOneEngine=" + onlyOneEngine +
        ", xVersion=" + xVersion +
        ", httpReadOnly=" + httpReadOnly +
        ", checkReferences=" + checkReferences +
        ", resolutionContext=" + resolutionContext +
        ", disableDefaultResourceFetcher=" + disableDefaultResourceFetcher +
        ", checkIpsCodes=" + checkIpsCodes +
        ", bundle=" + bundle +
        ", analyzeOutcomeWithAI=" + analyzeOutcomeWithAI +
        '}';
  }

	public List<Field> getValidateEngineParameters() {
		return Arrays.stream(this.getClass().getDeclaredFields())
			.filter(f -> f.isAnnotationPresent(JsonProperty.class))
			.filter(f -> !f.getName().equals("profile"))
			.filter(f -> f.getType() == String.class || f.getType() == boolean.class || f.getType() == String[].class)
			.collect(Collectors.toList());
	}

  public void addContextToExtension(final Extension ext) {
	addExtension(ext, "ig", new StringType(this.ig));
	addExtension(ext, "hintAboutNonMustSupport", new BooleanType(this.hintAboutNonMustSupport));
	addExtension(ext, "recursive", new BooleanType(this.recursive));

	addExtension(ext, "showMessagesFromReferences", new BooleanType(this.showMessagesFromReferences));
	addExtension(ext, "doDebug", new BooleanType(this.doDebug));
	addExtension(ext, "assumeValidRestReferences", new BooleanType(this.assumeValidRestReferences));
	addExtension(ext, "canDoNative", new BooleanType(this.canDoNative));
	addExtension(ext, "noExtensibleBindingMessages", new BooleanType(this.noExtensibleBindingMessages));
	addExtension(ext, "noUnicodeBiDiControlChars", new BooleanType(this.noUnicodeBiDiControlChars));
	addExtension(ext, "noInvariants", new BooleanType(this.noInvariants));
	addExtension(ext, "displayIssuesAreWarnings", new BooleanType(this.displayIssuesAreWarnings));
	addExtension(ext, "wantInvariantsInMessages", new BooleanType(this.wantInvariantsInMessages));
	addExtension(ext, "doImplicitFHIRPathStringConversion", new BooleanType(this.doImplicitFHIRPathStringConversion));
	// addExtension(ext, "htmlInMarkdownCheck", new BooleanType(this.htmlInMarkdownCheck == HtmlInMarkdownCheck.ERROR));

	addExtension(ext, "securityChecks", new BooleanType(this.securityChecks));
	addExtension(ext, "crumbTrails", new BooleanType(this.crumbTrails));
	addExtension(ext, "forPublication", new BooleanType(this.forPublication));
	addExtension(ext, "showMessageIds", new BooleanType(this.showMessageIds));
	addExtension(ext, "showTerminologyRouting", new BooleanType(this.showTerminologyRouting));
	addExtension(ext, "clearTxCache", new BooleanType(this.clearTxCache));
	addExtension(ext, "httpReadOnly", new BooleanType(this.httpReadOnly));
	addExtension(ext, "allowExampleUrls", new BooleanType(this.allowExampleUrls));
	addExtension(ext, "txServer", new UriType(this.txServer));
	addExtension(ext, "txServerCache", new BooleanType(this.txServerCache));
  addExtension(ext, "txLog", new StringType(this.txLog));
  addExtension(ext, "txUseEcosystem", new BooleanType(this.txUseEcosystem));
	addExtension(ext, "lang", new StringType(this.lang));
	addExtension(ext, "snomedCT", new StringType(this.snomedCT));
	addExtension(ext, "fhirVersion", new StringType(this.fhirVersion));
	addExtension(ext, "xVersion", new BooleanType(this.xVersion));
	addExtension(ext, "onlyOneEngine", new BooleanType(this.onlyOneEngine));
	addExtension(ext, "ig", new StringType(this.ig));
	// addExtension(ext, "questionnaireMode", new BooleanType(this.questionnaireMode));
	// addExtension(ext, "level", new BooleanType(this.level));
	// addExtension(ext, "mode", new BooleanType(this.mode));
	addExtension(ext, "locale", new StringType(this.locale));
	// addExtension(ext, "locations", new StringType(this.locations));
	addExtension(ext, "jurisdiction", new StringType(this.jurisdiction));
	addExtension(ext, "check-references", new BooleanType(this.checkReferences));
	addExtension(ext, "resolution-context", new StringType(this.resolutionContext));
	addExtension(ext, "disableDefaultResourceFetcher", new BooleanType(this.disableDefaultResourceFetcher));
	addExtension(ext, "check-ips-codes", new BooleanType(this.checkIpsCodes));
	addExtension(ext, "bundle", new StringType(this.bundle));
  addExtension(ext, "analyzeOutcomeWithAI", new BooleanType(this.analyzeOutcomeWithAI));
  for( var extension : this.extensions) {
    addExtension(ext, "extensions", new StringType(extension));
  }
  }
}
