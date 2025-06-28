package ch.ahdis.matchbox.engine.cli;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Locale;

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

import org.hl7.fhir.r5.model.ImplementationGuide;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.terminologies.JurisdictionUtilities;
import org.hl7.fhir.utilities.FileFormat;
import org.hl7.fhir.utilities.TimeTracker;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.validation.Scanner;
import org.hl7.fhir.validation.service.model.ValidationContext;
import org.hl7.fhir.validation.service.utils.EngineMode;
import org.hl7.fhir.validation.cli.Display;
import org.hl7.fhir.validation.cli.param.Params;
import org.hl7.fhir.validation.testexecutor.TestExecutor;
import org.hl7.fhir.validation.testexecutor.TestExecutorParams;

import ch.ahdis.matchbox.engine.MatchboxEngine;
import lombok.extern.slf4j.Slf4j;


/**
 * A executable class 
 * 
 * adapted from https://github.com/hapifhir/org.hl7.fhir.core/blob/master/org.hl7.fhir.validation/src/main/java/org/hl7/fhir/validation/ValidatorCli.java
 *
 * @author Oliver Egger
 */
@Slf4j
public class MatchboxCli {

  public static final String HTTP_PROXY_HOST = "http.proxyHost";
  public static final String HTTP_PROXY_PORT = "http.proxyPort";
  public static final String HTTP_PROXY_USER = "http.proxyUser";
  public static final String HTTP_PROXY_PASS = "http.proxyPassword";
  public static final String JAVA_DISABLED_TUNNELING_SCHEMES = "jdk.http.auth.tunneling.disabledSchemes";
  public static final String JAVA_DISABLED_PROXY_SCHEMES = "jdk.http.auth.proxying.disabledSchemes";
  public static final String JAVA_USE_SYSTEM_PROXIES = "java.net.useSystemProxies";

  private static MatchboxService matchboxService = new MatchboxService();

  public static void main(String[] args) throws Exception {
    TimeTracker tt = new TimeTracker();
    TimeTracker.Session tts = tt.start("Loading");
    
    System.out.println(VersionUtil.getPoweredBy());
    Display.displaySystemInfo(log);

    if (Params.hasParam(args, Params.PROXY)) {
      assert Params.getParam(args, Params.PROXY) != null : "PROXY arg passed in was NULL";
      String[] p = Params.getParam(args, Params.PROXY).split(":");
      System.setProperty(HTTP_PROXY_HOST, p[0]);
      System.setProperty(HTTP_PROXY_PORT, p[1]);
    }

    if (Params.hasParam(args, Params.PROXY_AUTH)) {
      assert Params.getParam(args, Params.PROXY) != null : "Cannot set PROXY_AUTH without setting PROXY...";
      assert Params.getParam(args, Params.PROXY_AUTH) != null : "PROXY_AUTH arg passed in was NULL...";
      String[] p = Params.getParam(args, Params.PROXY_AUTH).split(":");
      String authUser = p[0];
      String authPass = p[1];

      /*
       * For authentication, use java.net.Authenticator to set proxy's configuration and set the system properties
       * http.proxyUser and http.proxyPassword
       */
      Authenticator.setDefault(
        new Authenticator() {
          @Override
          public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(authUser, authPass.toCharArray());
          }
        }
      );

      System.setProperty(HTTP_PROXY_USER, authUser);
      System.setProperty(HTTP_PROXY_PASS, authPass);
      System.setProperty(JAVA_USE_SYSTEM_PROXIES, "true");

      /*
       * For Java 1.8 and higher you must set
       * -Djdk.http.auth.tunneling.disabledSchemes=
       * to make proxies with Basic Authorization working with https along with Authenticator
       */
      System.setProperty(JAVA_DISABLED_TUNNELING_SCHEMES, "");
      System.setProperty(JAVA_DISABLED_PROXY_SCHEMES, "");
    }

    ValidationContext validationContext = Params.loadValidationContext(args);

    FileFormat.checkCharsetAndWarnIfNotUTF8(System.out);

    if (shouldDisplayHelpToUser(args)) {
      Display.displayHelpDetails(log, "help/help.txt");
    } else if (Params.hasParam(args, Params.TEST)) {
      parseTestParamsAndExecute(args);
    }
    else {
      Display.printCliParamsAndInfo(log,args);
      doValidation(tt, tts, validationContext);
    }
  }

  protected static void parseTestParamsAndExecute(String[] args) {
    final String testModuleParam = Params.getParam(args, Params.TEST_MODULES);
    final String testClassnameFilter = Params.getParam(args, Params.TEST_NAME_FILTER);
    final String testCasesDirectory = Params.getParam(args, Params.TEST);
    final String txCacheDirectory = Params.getParam(args, Params.TERMINOLOGY_CACHE);
    assert TestExecutorParams.isValidModuleParam(testModuleParam) : "Invalid test module param: " + testModuleParam;
    final String[] moduleNamesArg = TestExecutorParams.parseModuleParam(testModuleParam);

    assert TestExecutorParams.isValidClassnameFilterParam(testClassnameFilter) : "Invalid regex for test classname filter: " + testClassnameFilter;

    new TestExecutor(moduleNamesArg).executeTests(testClassnameFilter, txCacheDirectory, testCasesDirectory);

    System.exit(0);
  }
  
  private static boolean shouldDisplayHelpToUser(String[] args) {
    return (args.length == 0
      || Params.hasParam(args, Params.HELP)
      || Params.hasParam(args, "?")
      || Params.hasParam(args, "-?")
      || Params.hasParam(args, "/?"));
  }

  private static void doValidation(TimeTracker tt, TimeTracker.Session tts, ValidationContext cliContext) throws Exception {
    if (cliContext.getSv() == null) {
      cliContext.setSv(matchboxService.determineVersion(cliContext));
    }
    if (cliContext.getJurisdiction() == null) {
      System.out.println("  Jurisdiction: None specified (locale = "+Locale.getDefault().getCountry()+")");      
      System.out.println("  Note that exceptions and validation failures may happen in the absense of a locale");      
    } else {
      System.out.println("  Jurisdiction: "+JurisdictionUtilities.displayJurisdiction(cliContext.getJurisdiction()));
    }

    System.out.println("Loading");
    // Comment this out because definitions filename doesn't necessarily contain version (and many not even be 14 characters long).
    // Version gets spit out a couple of lines later after we've loaded the context
    String definitions = "dev".equals(cliContext.getSv()) ? "hl7.fhir.r5.core#current" : VersionUtilities.packageForVersion(cliContext.getSv()) + "#" + VersionUtilities.getCurrentVersion(cliContext.getSv());
    
    MatchboxEngine validator = matchboxService.initializeValidator(cliContext, definitions, tt);
    tts.end();
    switch (cliContext.getMode()) {
      case TRANSFORM:
        matchboxService.transform(cliContext, validator);
        break;
      case COMPILE:
        matchboxService.compile(cliContext, validator);
        break;
      case NARRATIVE:
        matchboxService.generateNarrative(cliContext, validator);
        break;
      case SNAPSHOT:
        matchboxService.generateSnapshot(cliContext, validator);
        break;
      case CONVERT:
        matchboxService.convertSources(cliContext, validator);
        break;
      case FHIRPATH:
        matchboxService.evaluateFhirpath(cliContext, validator);
        break;
      case VERSION:
        matchboxService.transformVersion(cliContext, validator);
        break;
      case VALIDATION:
      case SCAN:
      default:
        for (String s : cliContext.getProfiles()) {
          if (!validator.getContext().hasResource(StructureDefinition.class, s) && !validator.getContext().hasResource(ImplementationGuide.class, s)) {
            System.out.println("  Fetch Profile from " + s);
            validator.loadProfile(cliContext.getLocations().getOrDefault(s, s));
          }
        }
        System.out.println("Validating");
        if (cliContext.getMode() == EngineMode.SCAN) {
          Scanner validationScanner = new Scanner(validator.getContext(), validator.getValidator(null), validator.getIgLoader(), validator.getFhirPathEngine());
          validationScanner.validateScan(cliContext.getOutput(), cliContext.getSources());
        } else {
          matchboxService.validateSources(cliContext, validator);
        }
        break;
    }
    System.out.println("Done. " + tt.report()+". Max Memory = "+Utilities.describeSize(Runtime.getRuntime().maxMemory()));
  }
}
