package org.hl7.fhir.validation.cli.param;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.terminologies.JurisdictionUtilities;
import org.hl7.fhir.r5.utils.validation.BundleValidationRule;
import org.hl7.fhir.r5.utils.validation.constants.BestPracticeWarningLevel;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.filesystem.ManagedFileAccess;
import org.hl7.fhir.utilities.validation.ValidationOptions.R5BundleRelativeReferencePolicy;
import org.hl7.fhir.validation.service.model.ValidationContext;
import org.hl7.fhir.validation.service.model.HtmlInMarkdownCheck;
import org.hl7.fhir.validation.service.ValidatorWatchMode;
import org.hl7.fhir.validation.service.utils.EngineMode;
import org.hl7.fhir.validation.service.utils.QuestionnaireMode;
import org.hl7.fhir.validation.service.utils.ValidationLevel;

public class Params {

  public static final String VERSION = "-version";
  public static final String ALT_VERSION = "-alt-version";
  public static final String OUTPUT = "-output";

  public static final String OUTPUT_SUFFIX = "-outputSuffix";
  public static final String LEVEL = "-level";
  public static final String HTML_OUTPUT = "-html-output";
  public static final String PROXY = "-proxy";

  public static final String HTTPS_PROXY = "-https-proxy";
  public static final String PROXY_AUTH = "-auth";
  public static final String PROFILE = "-profile";
  public static final String PROFILES = "-profiles";
  public static final String CONFIG = "-config";
  public static final String OPTION = "-option";
  public static final String OPTIONS = "-options";
  public static final String BUNDLE = "-bundle";
  public static final String QUESTIONNAIRE = "-questionnaire";
  public static final String NATIVE = "-native";
  public static final String ASSUME_VALID_REST_REF = "-assumeValidRestReferences";
  public static final String CHECK_REFERENCES = "-check-references";
  public static final String RESOLUTION_CONTEXT = "-resolution-context";
  public static final String DEBUG = "-debug";
  public static final String SCT = "-sct";
  public static final String RECURSE = "-recurse";
  public static final String SHOW_MESSAGES_FROM_REFERENCES = "-showReferenceMessages";
  public static final String LOCALE = "-locale";
  public static final String EXTENSION = "-extension";
  public static final String HINT_ABOUT_NON_MUST_SUPPORT = "-hintAboutNonMustSupport";
  public static final String TO_VERSION = "-to-version";
  public static final String TX_PACK = "-tx-pack";
  public static final String RE_PACK = "-re-package";
  public static final String PACKAGE_NAME = "-package-name";
  public static final String PIN = "-pin";
  public static final String EXPAND = "-expand";
  public static final String DO_NATIVE = "-do-native";
  public static final String NO_NATIVE = "-no-native";
  public static final String COMPILE = "-compile";
  public static final String CODEGEN = "-codegen";
  public static final String FACTORY = "-factory";
  public static final String TRANSFORM = "-transform";
  public static final String FORMAT = "-format";
  public static final String LANG_TRANSFORM = "-lang-transform";
  public static final String EXP_PARAMS = "-expansion-parameters";
  public static final String NARRATIVE = "-narrative";
  public static final String SNAPSHOT = "-snapshot";
  public static final String INSTALL = "-install";
  public static final String SCAN = "-scan";
  public static final String TERMINOLOGY = "-tx";
  public static final String TERMINOLOGY_LOG = "-txLog";
  public static final String TERMINOLOGY_CACHE = "-txCache";
  public static final String TERMINOLOGY_ROUTING = "-tx-routing";
  public static final String TERMINOLOGY_CACHE_CLEAR = "-clear-tx-cache";
  public static final String LOG = "-log";
  public static final String LANGUAGE = "-language";
  public static final String IMPLEMENTATION_GUIDE = "-ig";
  public static final String DEFINITION = "defn";
  public static final String MAP = "-map";
  public static final String X = "-x";
  public static final String CONVERT = "-convert";
  public static final String FHIRPATH = "-fhirpath";
  public static final String TEST = "-tests";
  public static final String TX_TESTS = "-txTests";
  public static final String AI_TESTS = "-aiTests";
  public static final String HELP = "help";
  public static final String COMPARE = "-compare";
  public static final String SPREADSHEET = "-spreadsheet";
  public static final String DESTINATION = "-dest";
  public static final String LEFT = "-left";
  public static final String RIGHT = "-right";
  public static final String NO_INTERNAL_CACHING = "-no-internal-caching";

  public static final String PRELOAD_CACHE = "-preload-cache";
  public static final String NO_EXTENSIBLE_BINDING_WARNINGS = "-no-extensible-binding-warnings";
  public static final String NO_UNICODE_BIDI_CONTROL_CHARS = "-no_unicode_bidi_control_chars";
  public static final String NO_INVARIANTS = "-no-invariants";
  public static final String DISPLAY_WARNINGS = "-display-issues-are-warnings";
  public static final String WANT_INVARIANTS_IN_MESSAGES = "-want-invariants-in-messages";
  public static final String SECURITY_CHECKS = "-security-checks";
  public static final String CRUMB_TRAIL = "-crumb-trails";
  public static final String SHOW_MESSAGE_IDS = "-show-message-ids";
  public static final String FOR_PUBLICATION = "-forPublication";
  public static final String AI_SERVICE = "-ai-service";
  public static final String VERBOSE = "-verbose";
  public static final String SHOW_TIMES = "-show-times";
  public static final String ALLOW_EXAMPLE_URLS = "-allow-example-urls";
  public static final String OUTPUT_STYLE = "-output-style";
  public static final String ADVSIOR_FILE = "-advisor-file";
  public static final String DO_IMPLICIT_FHIRPATH_STRING_CONVERSION = "-implicit-fhirpath-string-conversions";
  public static final String JURISDICTION = "-jurisdiction";
  public static final String HTML_IN_MARKDOWN = "-html-in-markdown";
  public static final String SRC_LANG = "-src-lang";
  public static final String TGT_LANG = "-tgt-lang";
  public static final String ALLOW_DOUBLE_QUOTES = "-allow-double-quotes-in-fhirpath";
  public static final String DISABLE_DEFAULT_RESOURCE_FETCHER = "-disable-default-resource-fetcher";
  public static final String CHECK_IPS_CODES = "-check-ips-codes";
  public static final String BEST_PRACTICE = "-best-practice";
  public static final String UNKNOWN_CODESYSTEMS_CAUSE_ERROR = "-unknown-codesystems-cause-errors";
  public static final String NO_EXPERIMENTAL_CONTENT = "-no-experimental-content";

  public static final String RUN_TESTS = "-run-tests";

  public static final String TEST_MODULES = "-test-modules";

  public static final String TEST_NAME_FILTER = "-test-classname-filter";
  public static final String SPECIAL = "-special";
  public static final String TARGET = "-target";
  public static final String SOURCE = "-source";
  public static final String INPUT = "-input";
  public static final String FILTER = "-filter";
  public static final String EXTERNALS = "-externals";
  public static final String MODE = "-mode";
  private static final String FHIR_SETTINGS_PARAM = "-fhir-settings";
  private static final String WATCH_MODE_PARAM = "-watch-mode";
  private static final String WATCH_SCAN_DELAY = "-watch-scan-delay";
  private static final String WATCH_SETTLE_TIME = "-watch-settle-time";
  public static final String NO_HTTP_ACCESS = "-no-http-access";
  public static final String AUTH_NONCONFORMANT_SERVERS = "-authorise-non-conformant-tx-servers";
  public static final String R5_REF_POLICY = "r5-bundle-relative-reference-policy";

  /**
   * Checks the list of passed in params to see if it contains the passed in param.
   *
   * @param args  Array of params to search.
   * @param param {@link String} param to search for.
   * @return {@link Boolean#TRUE} if the list contains the given param.
   */
  public static boolean hasParam(String[] args, String param) {
    return Arrays.asList(args).contains(param);
  }

  /**
   * Fetches the  value for the passed in param from the provided list of params.
   *
   * @param args  Array of params to search.
   * @param param {@link String} param keyword to search for.
   * @return {@link String} value for the provided param.
   */
  public static String getParam(String[] args, String param) {
    for (int i = 0; i < args.length - 1; i++) {
      if (args[i].equals(param)) return args[i + 1];
    }
    return null;
  }

  /**
   * TODO Don't do this all in one for loop. Use the above methods.
   */
  public static ValidationContext loadValidationContext(String[] args) throws Exception {
    ValidationContext validationContext = new ValidationContext();

    // load the parameters - so order doesn't matter
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals(VERSION)) {
        validationContext.setSv(VersionUtilities.getCurrentPackageVersion(args[++i]));
      } else if (args[i].equals(FHIR_SETTINGS_PARAM)) {
        final String fhirSettingsFilePath = args[++i];
        if (! ManagedFileAccess.file(fhirSettingsFilePath).exists()) {
          throw new Error("Cannot find fhir-settings file: " + fhirSettingsFilePath);
        }
        validationContext.setFhirSettingsFile(fhirSettingsFilePath);
      } else if (args[i].equals(OUTPUT)) {
        if (i + 1 == args.length)
          throw new Error("Specified -output without indicating output file");
        else
          validationContext.setOutput(args[++i]);
      } else if (args[i].equals(OUTPUT_SUFFIX)) {
        if (i + 1 == args.length)
          throw new Error("Specified -outputSuffix without indicating output suffix");
        else
          validationContext.setOutputSuffix(args[++i]);
      }
      else if (args[i].equals(HTML_OUTPUT)) {
        if (i + 1 == args.length)
          throw new Error("Specified -html-output without indicating output file");
        else
          validationContext.setHtmlOutput(args[++i]);
      } else if (args[i].equals(PROXY)) {
        i++; // ignore next parameter
      } else if (args[i].equals(PROXY_AUTH)) {
        i++;
      } else if (args[i].equals(HTTPS_PROXY)) {
        i++;
      } else if (args[i].equals(PROFILE)) {
        String p = null;
        if (i + 1 == args.length) {
          throw new Error("Specified -profile without indicating profile url");
        } else {
          p = args[++i];
          validationContext.addProfile(p);
        }
      } else if (args[i].equals(PROFILES)) {
        String p = null;
        if (i + 1 == args.length) {
          throw new Error("Specified -profiles without indicating profile urls");
        } else {
          p = args[++i];
          for (String s : p.split("\\,")) {
            validationContext.addProfile(s);
          }
        }
      } else if (args[i].equals(OPTION)) {
        String p = null;
        if (i + 1 == args.length) {
          throw new Error("Specified -option without indicating option value");
        } else {
          p = args[++i];
          validationContext.addOption(p);
        }
      } else if (args[i].equals(OPTIONS)) {
        String p = null;
        if (i + 1 == args.length) {
          throw new Error("Specified -options without indicating option values");
        } else {
          p = args[++i];
          for (String s : p.split("\\,")) {
            validationContext.addOption(s);
          }
        }
      } else if (args[i].equals(BUNDLE)) {
        String profile = null;
        String rule = null;
        if (i + 1 == args.length) {
          throw new Error("Specified -profile without indicating bundle rule ");
        } else {
          rule = args[++i];
        }
        if (i + 1 == args.length) {
          throw new Error("Specified -profile without indicating profile source");
        } else {
          profile = args[++i];
        }
        validationContext.getBundleValidationRules().add(new BundleValidationRule().setRule(rule).setProfile(profile));
      } else if (args[i].equals(QUESTIONNAIRE)) {
        if (i + 1 == args.length)
          throw new Error("Specified -questionnaire without indicating questionnaire mode");
        else {
          String questionnaireMode = args[++i];
          validationContext.setQuestionnaireMode(QuestionnaireMode.fromCode(questionnaireMode));
        }
      } else if (args[i].equals(LEVEL)) {
        if (i + 1 == args.length)
          throw new Error("Specified -level without indicating level mode");
        else {
          String q = args[++i];
          validationContext.setLevel(ValidationLevel.fromCode(q));
        }
      } else if (args[i].equals(MODE)) {
        if (i + 1 == args.length)
          throw new Error("Specified -mode without indicating mode");
        else {
          String q = args[++i];
          validationContext.getModeParams().add(q);
        }
      } else if (args[i].equals(INPUT)) {
        if (i + 1 == args.length)
          throw new Error("Specified -input without providing value");
        else {
          String inp = args[++i];
          validationContext.getInputs().add(inp);
        }
      } else if (args[i].equals(NATIVE)) {
        validationContext.setDoNative(true);
      } else if (args[i].equals(ASSUME_VALID_REST_REF)) {
        validationContext.setAssumeValidRestReferences(true);
      } else if (args[i].equals(CHECK_REFERENCES)) {
        validationContext.setCheckReferences(true);
      } else if (args[i].equals(RESOLUTION_CONTEXT)) {
        validationContext.setResolutionContext(args[++i]);
      } else if (args[i].equals(DEBUG)) {
        validationContext.setDoDebug(true);
      } else if (args[i].equals(SCT)) {
        validationContext.setSnomedCT(args[++i]);
      } else if (args[i].equals(RECURSE)) {
        validationContext.setRecursive(true);
      } else if (args[i].equals(SHOW_MESSAGES_FROM_REFERENCES)) {
        validationContext.setShowMessagesFromReferences(true);
      } else if (args[i].equals(DO_IMPLICIT_FHIRPATH_STRING_CONVERSION)) {
        validationContext.setDoImplicitFHIRPathStringConversion(true);
      } else if (args[i].equals(HTML_IN_MARKDOWN)) {
        if (i + 1 == args.length)
          throw new Error("Specified "+HTML_IN_MARKDOWN+" without indicating mode");
        else {
          String q = args[++i];
          if (!HtmlInMarkdownCheck.isValidCode(q)) {
            throw new Error("Specified "+HTML_IN_MARKDOWN+" with na invalid code - must be ignore, warning, or error");            
          } else {
            validationContext.setHtmlInMarkdownCheck(HtmlInMarkdownCheck.fromCode(q));
          }
        }
      } else if (args[i].equals(BEST_PRACTICE)) {
        if (i + 1 == args.length)
          throw new Error("Specified "+BEST_PRACTICE+" without indicating mode");
        else {
          String q = args[++i];
          validationContext.setBestPracticeLevel(readBestPractice(q));
        }
      } else if (args[i].equals(LOCALE)) {
        if (i + 1 == args.length) {
          throw new Error("Specified -locale without indicating locale");
        } else {
          validationContext.setLocale(Locale.forLanguageTag(args[++i]));
        }
      } else if (args[i].equals(EXTENSION)) {
        validationContext.getExtensions().add(args[++i]);
      } else if (args[i].equals(NO_INTERNAL_CACHING)) {
        validationContext.setNoInternalCaching(true);
      } else if (args[i].equals(NO_EXTENSIBLE_BINDING_WARNINGS)) {
        validationContext.setNoExtensibleBindingMessages(true);
      } else if (args[i].equals(ALLOW_DOUBLE_QUOTES)) {
        validationContext.setAllowDoubleQuotesInFHIRPath(true);
      } else if (args[i].equals(DISABLE_DEFAULT_RESOURCE_FETCHER)) {
        validationContext.setDisableDefaultResourceFetcher(true);
      } else if (args[i].equals(CHECK_IPS_CODES)) {
        validationContext.setCheckIPSCodes(true);
      } else if (args[i].equals(NO_UNICODE_BIDI_CONTROL_CHARS)) {
        validationContext.setNoUnicodeBiDiControlChars(true);
      } else if (args[i].equals(NO_INVARIANTS)) {
        validationContext.setNoInvariants(true);
      } else if (args[i].equals(DISPLAY_WARNINGS)) {
        validationContext.setDisplayWarnings(true);
      } else if (args[i].equals(WANT_INVARIANTS_IN_MESSAGES)) {
        validationContext.setWantInvariantsInMessages(true);
      } else if (args[i].equals(HINT_ABOUT_NON_MUST_SUPPORT)) {
        validationContext.setHintAboutNonMustSupport(true);
      } else if (args[i].equals(TO_VERSION)) {
        validationContext.setTargetVer(args[++i]);
        validationContext.setMode(EngineMode.VERSION);
      } else if (args[i].equals(PACKAGE_NAME)) {
        validationContext.setPackageName(args[++i]);
        if (!hasParam(args, "-re-package")) {
          validationContext.setMode(EngineMode.CODEGEN);
        }
      } else if (args[i].equals(TX_PACK)) {
        validationContext.setMode(EngineMode.RE_PACKAGE);
        String pn = args[++i];
        if (pn != null) {
          if (pn.contains(",")) {
            for (String s : pn.split("\\,")) {
              validationContext.getIgs().add(s);
            }
          } else {
            validationContext.getIgs().add(pn);
          }
        }
        validationContext.getModeParams().add("tx");
        validationContext.getModeParams().add("expansions");
      } else if (args[i].equals(RE_PACK)) {
        validationContext.setMode(EngineMode.RE_PACKAGE);
        String pn = args[++i];
        if (pn != null) {
          if (pn.contains(",")) {
            for (String s : pn.split("\\,")) {
              validationContext.getIgs().add(s);
            }
          } else {
            validationContext.getIgs().add(pn);
          }
        }
        validationContext.getModeParams().add("tx");
        validationContext.getModeParams().add("cnt");
        validationContext.getModeParams().add("api");
      } else if (args[i].equals(PIN)) {
        validationContext.getModeParams().add("pin");
      } else if (args[i].equals(EXPAND)) {
        validationContext.getModeParams().add("expand");
      } else if (args[i].equals(DO_NATIVE)) {
        validationContext.setCanDoNative(true);
      } else if (args[i].equals(NO_NATIVE)) {
        validationContext.setCanDoNative(false);
      } else if (args[i].equals(TRANSFORM)) {
        validationContext.setMap(args[++i]);
        validationContext.setMode(EngineMode.TRANSFORM);
      } else if (args[i].equals(FORMAT)) {
        validationContext.setFormat(FhirFormat.fromCode(args[++i]));
      } else if (args[i].equals(LANG_TRANSFORM)) {
        validationContext.setLangTransform(args[++i]);
        validationContext.setMode(EngineMode.LANG_TRANSFORM);
      } else if (args[i].equals(EXP_PARAMS)) {
        validationContext.setExpansionParameters(args[++i]);
      } else if (args[i].equals(COMPILE)) {
        validationContext.setMap(args[++i]);
        validationContext.setMode(EngineMode.COMPILE);
      } else if (args[i].equals(CODEGEN)) {
        validationContext.setMode(EngineMode.CODEGEN);
      } else if (args[i].equals(FACTORY)) {
        validationContext.setMode(EngineMode.FACTORY);
        validationContext.setSource(args[++i]);
      } else if (args[i].equals(NARRATIVE)) {
        validationContext.setMode(EngineMode.NARRATIVE);
      } else if (args[i].equals(SPREADSHEET)) {
        validationContext.setMode(EngineMode.SPREADSHEET);
      } else if (args[i].equals(SNAPSHOT)) {
        validationContext.setMode(EngineMode.SNAPSHOT);
      } else if (args[i].equals(INSTALL)) {
        validationContext.setMode(EngineMode.INSTALL);
      } else if (args[i].equals(RUN_TESTS)) {
        // TODO setBaseTestingUtils test directory
        validationContext.setMode(EngineMode.RUN_TESTS);
      } else if (args[i].equals(SECURITY_CHECKS)) {
        validationContext.setSecurityChecks(true);
      } else if (args[i].equals(CRUMB_TRAIL)) {
        validationContext.setCrumbTrails(true);
      } else if (args[i].equals(SHOW_MESSAGE_IDS)) {
        validationContext.setShowMessageIds(true);
      } else if (args[i].equals(FOR_PUBLICATION)) {
        validationContext.setForPublication(true);
      } else if (args[i].equals(AI_SERVICE)) {
        validationContext.setAIService(args[++i]);
      } else if (args[i].equals(R5_REF_POLICY)) {
        validationContext.setR5BundleRelativeReferencePolicy(R5BundleRelativeReferencePolicy.fromCode(args[++i]));
      } else if (args[i].equals(UNKNOWN_CODESYSTEMS_CAUSE_ERROR)) {
        validationContext.setUnknownCodeSystemsCauseErrors(true);
      } else if (args[i].equals(NO_EXPERIMENTAL_CONTENT)) {
        validationContext.setNoExperimentalContent(true);
      } else if (args[i].equals(VERBOSE)) {
        validationContext.setCrumbTrails(true);
        validationContext.setShowMessageIds(true);
      } else if (args[i].equals(ALLOW_EXAMPLE_URLS)) {
        String bl = args[++i]; 
        if ("true".equals(bl)) {
          validationContext.setAllowExampleUrls(true);
        } else if ("false".equals(bl)) {
          validationContext.setAllowExampleUrls(false);
        } else {
          throw new Error("Value for "+ALLOW_EXAMPLE_URLS+" not understood: "+bl);          
        }          
      } else if (args[i].equals(TERMINOLOGY_ROUTING)) {
        validationContext.setShowTerminologyRouting(true);
      } else if (args[i].equals(TERMINOLOGY_CACHE_CLEAR)) {
        validationContext.setClearTxCache(true);
      } else if (args[i].equals(SHOW_TIMES)) {
        validationContext.setShowTimes(true);
      } else if (args[i].equals(OUTPUT_STYLE)) {
        validationContext.setOutputStyle(args[++i]);
      } else if (args[i].equals(ADVSIOR_FILE)) {
        validationContext.setAdvisorFile(args[++i]);
        File f = ManagedFileAccess.file(validationContext.getAdvisorFile());
        if (!f.exists()) {
          throw new Error("Cannot find advisor file "+ validationContext.getAdvisorFile());
        } else if (!Utilities.existsInList(Utilities.getFileExtension(f.getName()), "json", "txt")) {
          throw new Error("Advisor file "+ validationContext.getAdvisorFile()+" must be a .json or a .txt file");
        }
      } else if (args[i].equals(SCAN)) {
        validationContext.setMode(EngineMode.SCAN);
      } else if (args[i].equals(TERMINOLOGY)) {
        if (i + 1 == args.length)
          throw new Error("Specified -tx without indicating terminology server");
        else {
          validationContext.setTxServer("n/a".equals(args[++i]) ? null : args[i]);
          validationContext.setNoEcosystem(true);
        }
      } else if (args[i].equals(TERMINOLOGY_LOG)) {
        if (i + 1 == args.length)
          throw new Error("Specified -txLog without indicating file");
        else
          validationContext.setTxLog(args[++i]);
      } else if (args[i].equals(TERMINOLOGY_CACHE)) {
        if (i + 1 == args.length)
          throw new Error("Specified -txCache without indicating file");
        else
          validationContext.setTxCache(args[++i]);
      } else if (args[i].equals(LOG)) {
        if (i + 1 == args.length)
          throw new Error("Specified -log without indicating file");
        else
          validationContext.setMapLog(args[++i]);
      } else if (args[i].equals(LANGUAGE)) {
        if (i + 1 == args.length)
          throw new Error("Specified -language without indicating language");
        else
          validationContext.setLang(args[++i]);
      } else if (args[i].equals(SRC_LANG)) {
        if (i + 1 == args.length)
          throw new Error("Specified -src-lang without indicating file");
        else
          validationContext.setSrcLang(args[++i]);
      } else if (args[i].equals(TGT_LANG)) {
        if (i + 1 == args.length)
          throw new Error("Specified -tgt-lang without indicating file");
        else
          validationContext.setTgtLang(args[++i]);
      } else if (args[i].equals(JURISDICTION)) {
        if (i + 1 == args.length)
          throw new Error("Specified -jurisdiction without indicating jurisdiction");
        else
          validationContext.setJurisdiction(processJurisdiction(args[++i]));
      } else if (args[i].equals(IMPLEMENTATION_GUIDE) || args[i].equals(DEFINITION)) {
        if (i + 1 == args.length)
          throw new Error("Specified " + args[i] + " without indicating ig file");
        else {
          String s = args[++i];
          String version = getVersionFromIGName(null, s);
          if (version == null) {
            validationContext.addIg(s);
          } else {
            String v = getParam(args, VERSION);
            if (v != null && !v.equals(version)) {
              throw new Error("Parameters are inconsistent: specified version is "+v+" but -ig parameter "+s+" implies a different version");
            } else if (validationContext.getSv() != null && !version.equals(validationContext.getSv())) {
              throw new Error("Parameters are inconsistent: multiple -ig parameters implying differetion versions ("+ validationContext.getSv()+","+version+")");
            } else {
              validationContext.setSv(version);
            }
          }
        }
      } else if (args[i].equals(ALT_VERSION)) {
        if (i + 1 == args.length)
          throw new Error("Specified " + args[i] + " without indicating version");
        else {
          String s = args[++i];
          String v = VersionUtilities.getMajMin(s);
          if (v == null) {
            throw new Error("Unsupported FHIR Version "+s);
          }
          String pid = VersionUtilities.packageForVersion(v);
          pid = pid + "#"+VersionUtilities.getCurrentPackageVersion(v);
          validationContext.addIg(pid);
        }
      } else if (args[i].equals(MAP)) {
        if (validationContext.getMap() == null) {
          if (i + 1 == args.length)
            throw new Error("Specified -map without indicating map file");
          else
            validationContext.setMap(args[++i]);
        } else {
          throw new Exception("Can only nominate a single -map parameter");
        }
      } else if (args[i].equals(WATCH_MODE_PARAM)) {
        if (i + 1 == args.length) {
          throw new Error("Specified -watch-mode without indicating mode value");
        } else {
          validationContext.setWatchMode(readWatchMode(args[++i]));
        }
      } else if (args[i].equals(WATCH_SCAN_DELAY)) {
        if (i + 1 == args.length) {
          throw new Error("Specified -watch-scan-delay without indicating mode value");
        } else {
          validationContext.setWatchScanDelay(readInteger(WATCH_SCAN_DELAY, args[++i]));
        }
      } else if (args[i].equals(WATCH_SETTLE_TIME)) {
          if (i + 1 == args.length) {
            throw new Error("Specified -watch-mode without indicating mode value");
          } else {
            validationContext.setWatchSettleTime(readInteger(WATCH_SETTLE_TIME, args[++i]));
          }      } else if (args[i].startsWith(X)) {
        i++;
      } else if (args[i].equals(CONVERT)) {
        validationContext.setMode(EngineMode.CONVERT);
      } else if (args[i].equals(FHIRPATH)) {
        validationContext.setMode(EngineMode.FHIRPATH);
        if (validationContext.getFhirpath() == null)
          if (i + 1 == args.length)
            throw new Error("Specified -fhirpath without indicating a FHIRPath expression");
          else
            validationContext.setFhirpath(args[++i]);
        else
          throw new Exception("Can only nominate a single -fhirpath parameter");
      } else if (!Utilities.existsInList(args[i], AUTH_NONCONFORMANT_SERVERS)) {
        validationContext.addSource(args[i]);
      }
    }
    
    return validationContext;
  }

  private static BestPracticeWarningLevel readBestPractice(String s) {
    if (s == null) {
      return BestPracticeWarningLevel.Warning;
    }
    switch (s.toLowerCase()) {
    case "warning" : return BestPracticeWarningLevel.Warning;
    case "error" : return BestPracticeWarningLevel.Error;
    case "hint" : return BestPracticeWarningLevel.Hint;
    case "ignore" : return BestPracticeWarningLevel.Ignore;
    case "w" : return BestPracticeWarningLevel.Warning;
    case "e" : return BestPracticeWarningLevel.Error;
    case "h" : return BestPracticeWarningLevel.Hint;
    case "i" : return BestPracticeWarningLevel.Ignore;
    }
    throw new Error("The best-practice level ''"+s+"'' is not valid");
  }

  private static int readInteger(String name, String value) {
    if (!Utilities.isInteger(value)) {
      throw new Error("Unable to read "+value+" provided for '"+name+"' - must be an integer");
    }
    return Integer.parseInt(value);
  }

  private static ValidatorWatchMode readWatchMode(String s) {
    if (s == null) {
      return ValidatorWatchMode.NONE;
    }
    switch (s.toLowerCase()) {
    case "all" : return ValidatorWatchMode.ALL;
    case "none" : return ValidatorWatchMode.NONE;
    case "single" : return ValidatorWatchMode.SINGLE;
    case "a" : return ValidatorWatchMode.ALL;
    case "n" : return ValidatorWatchMode.NONE;
    case "s" : return ValidatorWatchMode.SINGLE;
    }
    throw new Error("The watch mode ''"+s+"'' is not valid");
  }

  private static String processJurisdiction(String s) {
    if (s.startsWith("urn:iso:std:iso:3166#") || s.startsWith("urn:iso:std:iso:3166:-2#") || s.startsWith("http://unstats.un.org/unsd/methods/m49/m49.htm#")) {
      return s;
    } else {
      String v = JurisdictionUtilities.getJurisdictionFromLocale(s);
      if (v != null) { 
        return v;        
      } else {
        throw new FHIRException("Unable to understand Jurisdiction '"+s+"'");
      }
    }
  }

  public static String getTerminologyServerLog(String[] args) throws IOException {
    String txLog = null;
    if (hasParam(args, "-txLog")) {
      txLog = getParam(args, "-txLog");
      ManagedFileAccess.file(txLog).delete();
    }
    return txLog;
  }

  public static void checkIGFileReferences(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if (IMPLEMENTATION_GUIDE.equals(args[i])) {
        if (i + 1 == args.length)
          throw new Error("Specified -ig without indicating ig file");
        else {
          String s = args[++i];
          if (!s.startsWith("hl7.fhir.core-")) {
            System.out.println("Load Package: " + s);
          }
        }
      }
    }
  }

  public static String getVersion(String[] args) {
    String v = Params.getParam(args, "-version");
    if (v == null) {
      v = "5.0";
      for (int i = 0; i < args.length; i++) {
        if ("-ig".equals(args[i])) {
          if (i + 1 == args.length)
            throw new Error("Specified -ig without indicating ig file");
          else {
            String n = args[i + 1];
            v = getVersionFromIGName(v, n);
          }
        }
      }
    } else if (VersionUtilities.isR2Ver(v)) {
      v = "1.0";
    } else if (VersionUtilities.isR2BVer(v)) {
      v = "1.4";
    } else if (VersionUtilities.isR3Ver(v)) {
      v = "3.0";
    } else if (VersionUtilities.isR4Ver(v)) {
      v = "4.0";
    } else if (VersionUtilities.isR4BVer(v)) {
      v = "4.3";
    } else if (VersionUtilities.isR5Ver(v)) {
      v = "5.0";
    } else if (VersionUtilities.isR6Ver(v)) {
      v = "6.0";
    }
    return v;
  }

  /**
   * Evaluates the current implementation guide file name and sets the current version accordingly.
   * <p>
   * If igFileName is not one of the known patterns, will return whatever value is passed in as default.
   *
   * @param defaultValue Version to return if no associated version can be determined from passed in igFileName
   * @param igFileName   Name of the implementation guide
   * @return
   */
  public static String getVersionFromIGName(String defaultValue, String igFileName) {
    if (igFileName.equals("hl7.fhir.core")) {
      defaultValue = "5.0";
    } else if (igFileName.startsWith("hl7.fhir.core#")) {
      defaultValue = VersionUtilities.getCurrentPackageVersion(igFileName.substring(14));
    } else if (igFileName.startsWith("hl7.fhir.r2.core#") || igFileName.equals("hl7.fhir.r2.core")) {
      defaultValue = "1.0";
    } else if (igFileName.startsWith("hl7.fhir.r2b.core#") || igFileName.equals("hl7.fhir.r2b.core")) {
      defaultValue = "1.4";
    } else if (igFileName.startsWith("hl7.fhir.r3.core#") || igFileName.equals("hl7.fhir.r3.core")) {
      defaultValue = "3.0";
    } else if (igFileName.startsWith("hl7.fhir.r4.core#") || igFileName.equals("hl7.fhir.r4.core")) {
      defaultValue = "4.0";
    } else if (igFileName.startsWith("hl7.fhir.r5.core#") || igFileName.equals("hl7.fhir.r5.core")) {
      defaultValue = "5.0";
    } else if (igFileName.startsWith("hl7.fhir.r6.core#") || igFileName.equals("hl7.fhir.r6.core")) {
      defaultValue = "6.0";
    }
    return defaultValue;
  }
}
