package ch.ahdis.matchbox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.hl7.fhir.r5.terminologies.JurisdictionUtilities;
import org.hl7.fhir.r5.utils.validation.BundleValidationRule;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.validation.cli.model.HtmlInMarkdownCheck;
import org.hl7.fhir.validation.cli.utils.EngineMode;
import org.hl7.fhir.validation.cli.utils.QuestionnaireMode;
import org.hl7.fhir.validation.cli.utils.ValidationLevel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A POJO for storing the flags/values for the CLI validator.
 * Needed to copy the class because the setters with CliContext as return type are not accessible via reflection
 * In addition we have parameters from the CliContext which do not make sense to expose for the Web APi
 */
public class CliContext {

  @JsonProperty("doNative")
  private boolean doNative = false;
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
  @JsonProperty("noInternalCaching")
  private boolean noInternalCaching = false; // internal, for when debugging terminology validation
  @JsonProperty("noExtensibleBindingMessages")
  private boolean noExtensibleBindingMessages = false;
  @JsonProperty("noUnicodeBiDiControlChars")
  private boolean noUnicodeBiDiControlChars = false;
  @JsonProperty("noInvariants")
  private boolean noInvariants = false;
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
  private String txServer = "http://tx.fhir.org";
  // @JsonProperty("sv")
  // private String sv = null;
  // @JsonProperty("txLog")
  // private String txLog = null;
  // @JsonProperty("txCache")
  // private String txCache = null;
  // @JsonProperty("mapLog")
  // private String mapLog = null;
  @JsonProperty("lang")
  private String lang = null;
  // @JsonProperty("fhirpath")
  // private String fhirpath = null;
  @JsonProperty("snomedCT")
  private String snomedCT = "900000000000207008";
  @JsonProperty("targetVer")
  private String targetVer = null;

  // @JsonProperty("extensions")
  // private List<String> extensions = new ArrayList<String>();
  @JsonProperty("igs")
  private List<String> igs = new ArrayList<String>();
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
  
  @JsonProperty("allowExampleUrls")
  private boolean allowExampleUrls = false;
  
  // @JsonProperty("showTimes")
  // private boolean showTimes = false;
  
  @JsonProperty("locale")
  private String locale = Locale.ENGLISH.getDisplayLanguage();

  @JsonProperty("locations")
  private Map<String, String> locations = new HashMap<String, String>();

  // @JsonProperty("outputStyle")
  // private String outputStyle = null;
  
  // TODO: Mark what goes here?
  // private List<BundleValidationRule> bundleValidationRules = new ArrayList<>();

  @JsonProperty("jurisdiction")
  private String jurisdiction = JurisdictionUtilities.getJurisdictionFromLocale(Locale.getDefault().getCountry());



  @JsonProperty("igs")
  public List<String> getIgs() {
    return igs;
  }

  @JsonProperty("igs")
  public void setIgs(List<String> igs) {
    this.igs = igs;
  }


  public CliContext addIg(String ig) {
    if (this.igs == null) {
      this.igs = new ArrayList<>();
    }
    this.igs.add(ig);
    return this;
  }

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
  public String getLanguageCode() {
    return locale;
  }

  public Locale getLocale() {
    return Locale.forLanguageTag(this.locale);
  }

  @JsonProperty("locale")
  public void setLocale(String languageString) {
    this.locale = languageString;
  }

  @JsonProperty("locale")
  public void setLocale(Locale locale) {
    this.locale = locale.getLanguage();
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
  public String getSnomedCTCode() {
    if ("intl".equals(snomedCT)) return "900000000000207008";
    if ("us".equals(snomedCT)) return "731000124108";
    if ("uk".equals(snomedCT)) return "999000041000000102";
    if ("au".equals(snomedCT)) return "32506021000036107";
    if ("ca".equals(snomedCT)) return "20611000087101";
    if ("nl".equals(snomedCT)) return "11000146104";
    if ("se".equals(snomedCT)) return "45991000052106";
    if ("es".equals(snomedCT)) return "449081005";
    if ("dk".equals(snomedCT)) return "554471000005108";
    return snomedCT;
  }

  @JsonProperty("snomedCT")
  public void setSnomedCT(String snomedCT) {
    this.snomedCT = snomedCT;
  }

  @JsonProperty("targetVer")
  public String getTargetVer() {
    return targetVer;
  }

  @JsonProperty("targetVer")
  public void setTargetVer(String targetVer) {
    this.targetVer = targetVer;
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

  @JsonProperty("noInternalCaching")
  public boolean isNoInternalCaching() {
    return noInternalCaching;
  }

  @JsonProperty("noInternalCaching")
  public void setNoInternalCaching(boolean noInternalCaching) {
    this.noInternalCaching = noInternalCaching;
  }

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    CliContext that = (CliContext) o;
    return doNative == that.doNative &&
      hintAboutNonMustSupport == that.hintAboutNonMustSupport &&
      recursive == that.recursive &&
      doDebug == that.doDebug &&
      assumeValidRestReferences == that.assumeValidRestReferences &&
      canDoNative == that.canDoNative &&
      noInternalCaching == that.noInternalCaching &&
      noExtensibleBindingMessages == that.noExtensibleBindingMessages &&
      noUnicodeBiDiControlChars == that.noUnicodeBiDiControlChars &&
      noInvariants == that.noInvariants &&
      wantInvariantsInMessages == that.wantInvariantsInMessages &&
      Objects.equals(txServer, that.txServer) &&
      Objects.equals(lang, that.lang) &&
      Objects.equals(snomedCT, that.snomedCT) &&
      Objects.equals(targetVer, that.targetVer) &&
      Objects.equals(igs, that.igs) &&
      Objects.equals(questionnaireMode, that.questionnaireMode) &&
      Objects.equals(level, that.level) &&
      Objects.equals(crumbTrails, that.crumbTrails) &&
      Objects.equals(forPublication, that.forPublication) &&
      Objects.equals(allowExampleUrls, that.allowExampleUrls) &&
      mode == that.mode &&
      Objects.equals(locale, that.locale) &&
      Objects.equals(jurisdiction, that.jurisdiction) &&
      Objects.equals(locations, that.locations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(doNative,  hintAboutNonMustSupport, recursive, doDebug, assumeValidRestReferences, canDoNative, noInternalCaching, 
            noExtensibleBindingMessages, noInvariants, wantInvariantsInMessages, txServer, lang, snomedCT,
            targetVer, igs, questionnaireMode, level, mode, locale, locations, crumbTrails, forPublication, allowExampleUrls, jurisdiction, noUnicodeBiDiControlChars);
  }

  @Override
  public String toString() {
    return "Parameters {" +
      "doNative=" + doNative +
      ", hintAboutNonMustSupport=" + hintAboutNonMustSupport +
      ", recursive=" + recursive +
      ", doDebug=" + doDebug +
      ", assumeValidRestReferences=" + assumeValidRestReferences +
      ", canDoNative=" + canDoNative +
      ", noInternalCaching=" + noInternalCaching +
      ", noExtensibleBindingMessages=" + noExtensibleBindingMessages +
      ", noUnicodeBiDiControlChars=" + noUnicodeBiDiControlChars +
      ", noInvariants=" + noInvariants +
      ", wantInvariantsInMessages=" + wantInvariantsInMessages +
      ", txServer='" + txServer + '\'' +
      ", lang='" + lang + '\'' +
      ", snomedCT='" + snomedCT + '\'' +
      ", targetVer='" + targetVer + '\'' +
      ", igs=" + igs +
      ", questionnaireMode=" + questionnaireMode +
      ", level=" + level +
      ", mode=" + mode +
      ", securityChecks=" + securityChecks +
      ", crumbTrails=" + crumbTrails +
      ", forPublication=" + forPublication +
      ", jurisdiction=" + jurisdiction +
      ", allowExampleUrls=" + allowExampleUrls +
      ", locale='" + locale + '\'' +
      ", locations=" + locations +
      '}';
  }
}