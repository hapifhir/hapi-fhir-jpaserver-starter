package ch.ahdis.validation;

import static org.junit.Assert.assertEquals;

import java.io.File;
/**
 * Use the ValidationTests defined from 
 */
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.test.utils.TestingUtilities;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.utilities.validation.ValidationMessage.IssueSeverity;
import org.hl7.fhir.validation.ValidationEngine;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.core.io.ClassPathResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@RunWith(Parameterized.class)
public class CoreValidationTests  {

  public final static boolean PRINT_OUTPUT_TO_CONSOLE = true;

  private String targetServer = "http://localhost:8080/hapi-fhir-jpavalidator/fhir";

  @Parameters(name = "{index}: id {0}")
  public static Iterable<Object[]> data() throws IOException {
    String contents = TestingUtilities.loadTestResource("validator", "manifest.json");

    Map<String, JsonObject> examples = new HashMap<String, JsonObject>();
    manifest = (JsonObject) new com.google.gson.JsonParser().parse(contents);
    for (Entry<String, JsonElement> e : manifest.getAsJsonObject("test-cases").entrySet()) {
      JsonObject content = e.getValue().getAsJsonObject();
      String version = "5.0";
      List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
      if (content.has("version")) {
        version = content.get("version").getAsString();
      }
      if ("4.0".equals(version)) {
          examples.put(e.getKey(), content);
      }  
    }

    List<String> names = new ArrayList<String>(examples.size());
    names.addAll(examples.keySet());
    Collections.sort(names);

    List<Object[]> objects = new ArrayList<Object[]>(examples.size());
    for (String id : names) {
      objects.add(new Object[]{id, examples.get(id)});
    }
    return objects;
  }

  private static JsonObject manifest;
  private JsonObject content;
  private String version;
  private String name;

  private static final String DEF_TX = "http://tx.fhir.org";
//  private static final String DEF_TX = "http://local.fhir.org:960";
  private static Map<String, ValidationEngine> ve = new HashMap<>();
  private static ValidationEngine vCurr;

  public CoreValidationTests(String name, JsonObject content) {
    this.name = name;
    this.content = content;
  }

  @SuppressWarnings("deprecation")
  @Test
  @Ignore
  public void test() throws Exception {
    long setup = System.nanoTime();
    this.content = content;
    this.name = name;
    System.out.println("---- " + name + " ----------------------------------------------------------------");
    System.out.println("** Core: ");
    String txLog = null;
    if (content.has("txLog")) {
      txLog = content.get("txLog").getAsString();
    }

    if (content.has("use-test") && !content.get("use-test").getAsBoolean())
      Assume.assumeTrue(false);

    if (content.has("packages")) 
      Assume.assumeTrue(false);

    if (content.has("supporting")) 
      Assume.assumeTrue(false);

    switch(name) {
      case "bb-obs-value-is-not-in-valueset.json":
        // Profile reference 'https://bb/StructureDefinition/BBDemographicAge' could not be resolved (expected 0 error, would need to load up profile?)
      case "bundle-duplicate-ids-not.json":
      // Failed to call access method: com.google.gson.JsonSyntaxException: com.google.gson.stream.MalformedJsonException: Use JsonReader.setLenient(true) to accept malformed JSON at line 2805 column 2 path $"
      case "bundle-bad-by-param.xml":
      // should be correct without a profile but indicates an error - core validator has no error
      case "obs-temp.json":
//      "diagnostics": "Profile reference 'https://fhir.cambio.se/StructureDefinition/ObservationBodyTemperaturePrehospital/v1' could not be resolved, so has not been checke

      Assume.assumeTrue(false);
      break;
    }

    String testCaseContent = TestingUtilities.loadTestResource("validator", name);

    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    ValidationClient validationClient = new ValidationClient(contextR4, this.targetServer);

    IBaseOperationOutcome operationOutcome = validationClient.validate(testCaseContent, null);

    JsonObject java = content.getAsJsonObject("java");
    Integer errorCount = java.get("errorCount").getAsInt();
    assertEquals(errorCount, new Integer(IgValidateR4TestStandalone.getValidationFailures((OperationOutcome) operationOutcome)));

/*     if (content.has("supporting")) {
      for (JsonElement e : content.getAsJsonArray("supporting")) {
        String filename = e.getAsString();
        String contents = TestingUtilities.loadTestResource("validator", filename);
        CanonicalResource mr = (CanonicalResource) loadResource(filename, contents);
        val.getContext().cacheResource(mr);
        if (mr instanceof ImplementationGuide) {
          val.getImplementationGuides().add((ImplementationGuide) mr);
        }
      }
    }
 */ 
    //val.getBundleValidationRules().clear();
/*     if (content.has("bundle-param")) {
      val.getBundleValidationRules().add(new BundleValidationRule(content.getAsJsonObject("bundle-param").get("rule").getAsString(), content.getAsJsonObject("bundle-param").get("profile").getAsString()));
    }
    if (content.has("profiles")) {
      for (JsonElement je : content.getAsJsonArray("profiles")) {
        String filename = je.getAsString();
        String contents = TestingUtilities.loadTestResource("validator", filename);
        StructureDefinition sd = loadProfile(filename, contents, messages);
        val.getContext().cacheResource(sd);
      }
    }
*/
//     List<ValidationMessage> errors = new ArrayList<ValidationMessage>();
/*     if (content.getAsJsonObject("java").has("debug")) {
      val.setDebug(content.getAsJsonObject("java").get("debug").getAsBoolean());
    } else {
      val.setDebug(false);
    } */
/*     if (content.has("best-practice")) {
      val.setBestPracticeWarningLevel(BestPracticeWarningLevel.valueOf(content.get("best-practice").getAsString()));
    }
    if (content.has("examples")) {
      val.setAllowExamples(content.get("examples").getAsBoolean());
    } else {
      val.setAllowExamples(true);
    }
    if (content.has("security-checks")) {
      val.setSecurityChecks(content.get("security-checks").getAsBoolean());
    } */
//     if (content.has("logical")==false) {
//      val.setAssumeValidRestReferences(content.has("assumeValidRestReferences") ? content.get("assumeValidRestReferences").getAsBoolean() : false);
/*       System.out.println(String.format("Start Validating (%d to set up)", (System.nanoTime() - setup) / 1000000));
      if (name.endsWith(".json"))
        val.validate(null, errors, IOUtils.toInputStream(testCaseContent, Charsets.UTF_8), FhirFormat.JSON);
      else
        val.validate(null, errors, IOUtils.toInputStream(testCaseContent, Charsets.UTF_8), FhirFormat.XML);
      System.out.println(val.reportTimes());
      checkOutcomes(errors, content, null, name);
 */    
//} 
/*     if (content.has("profile")) {
      System.out.print("** Profile: ");
      JsonObject profile = content.getAsJsonObject("profile");
      if (profile.getAsJsonObject("java").has("debug")) {
        val.setDebug(profile.getAsJsonObject("java").get("debug").getAsBoolean());
      }
      if (profile.has("supporting")) {
        for (JsonElement e : profile.getAsJsonArray("supporting")) {
          String filename = e.getAsString();
          String contents = TestingUtilities.loadTestResource("validator", filename);
          CanonicalResource mr = (CanonicalResource) loadResource(filename, contents);
          val.getContext().cacheResource(mr);
          if (mr instanceof ImplementationGuide) {
            val.getImplementationGuides().add((ImplementationGuide) mr);
          }
        }
      }
      String filename = profile.get("source").getAsString();
      String contents = TestingUtilities.loadTestResource("validator", filename);
      System.out.println("Name: " + name + " - profile : " + profile.get("source").getAsString());
      version = content.has("version") ? content.get("version").getAsString() : Constants.VERSION;
      StructureDefinition sd = loadProfile(filename, contents, messages);
      val.getContext().cacheResource(sd);
      val.setAssumeValidRestReferences(profile.has("assumeValidRestReferences") ? profile.get("assumeValidRestReferences").getAsBoolean() : false);
      List<ValidationMessage> errorsProfile = new ArrayList<ValidationMessage>();
      if (name.endsWith(".json"))
        val.validate(null, errorsProfile, IOUtils.toInputStream(testCaseContent, Charsets.UTF_8), FhirFormat.JSON, asSdList(sd));
      else
        val.validate(null, errorsProfile, IOUtils.toInputStream(testCaseContent, Charsets.UTF_8), FhirFormat.XML, asSdList(sd));
      System.out.println(val.reportTimes());
      checkOutcomes(errorsProfile, profile, filename, name);
    } */
   /*  if (content.has("logical")) {
      System.out.print("** Logical: ");

      JsonObject logical = content.getAsJsonObject("logical");
      if (logical.has("supporting")) {
        for (JsonElement e : logical.getAsJsonArray("supporting")) {
          String filename = e.getAsString();
          String contents = TestingUtilities.loadTestResource("validator", filename);
          CanonicalResource mr = (CanonicalResource) loadResource(filename, contents);
          if (mr instanceof StructureDefinition) {
            val.getContext().generateSnapshot((StructureDefinition) mr, true);
          }
          val.getContext().cacheResource(mr);
        }
      }
      if (logical.has("packages")) {
        for (JsonElement e : logical.getAsJsonArray("packages")) {
          vCurr.loadIg(e.getAsString(), true);
        }
      }
      List<ValidationMessage> errorsLogical = new ArrayList<ValidationMessage>();
      Element le = val.validate(null, errorsLogical, IOUtils.toInputStream(testCaseContent, Charsets.UTF_8), (name.endsWith(".json")) ? FhirFormat.JSON : FhirFormat.XML);
      if (logical.has("expressions")) {
        FHIRPathEngine fp = new FHIRPathEngine(val.getContext());
        for (JsonElement e : logical.getAsJsonArray("expressions")) {
          String exp = e.getAsString();
          Assert.assertTrue(fp.evaluateToBoolean(null, le, le, le, fp.parse(exp)));
        }
      }
      checkOutcomes(errorsLogical, logical, "logical", name);
    } */
  }

  private List<StructureDefinition> asSdList(StructureDefinition sd) {
    List<StructureDefinition> res = new ArrayList<StructureDefinition>();
    res.add(sd);
    return res;
  }

  private void checkOutcomes(List<ValidationMessage> errors, JsonObject focus, String profile, String name) {
    JsonObject java = focus.getAsJsonObject("java");
    int ec = 0;
    int wc = 0;
    int hc = 0;
    List<String> errLocs = new ArrayList<>();
    for (ValidationMessage vm : errors) {
      if (vm.getLevel() == IssueSeverity.FATAL || vm.getLevel() == IssueSeverity.ERROR) {
        ec++;
        if (PRINT_OUTPUT_TO_CONSOLE) {
          System.out.println(vm.getDisplay());
        }
        errLocs.add(vm.getLocation());
      }
      if (vm.getLevel() == IssueSeverity.WARNING) {
        wc++;
        if (PRINT_OUTPUT_TO_CONSOLE) {
          System.out.println(vm.getDisplay());
        }
      }
      if (vm.getLevel() == IssueSeverity.INFORMATION) {
        hc++;
        if (PRINT_OUTPUT_TO_CONSOLE) {
          System.out.println(vm.getDisplay());
        }
      }
    }
    if (!TestingUtilities.context(version).isNoTerminologyServer() || !focus.has("tx-dependent")) {
      Assert.assertEquals("Test " + name + (profile == null ? "" : " profile: "+ profile) + ": Expected " + Integer.toString(java.get("errorCount").getAsInt()) + " errors, but found " + Integer.toString(ec) + ".", java.get("errorCount").getAsInt(), ec);
      if (java.has("warningCount")) {
        Assert.assertEquals( "Test " + name + (profile == null ? "" : " profile: "+ profile) + ": Expected " + Integer.toString(java.get("warningCount").getAsInt()) + " warnings, but found " + Integer.toString(wc) + ".", java.get("warningCount").getAsInt(), wc);
      }
      if (java.has("infoCount")) {
        Assert.assertEquals( "Test " + name + (profile == null ? "" : " profile: "+ profile) + ": Expected " + Integer.toString(java.get("infoCount").getAsInt()) + " hints, but found " + Integer.toString(hc) + ".", java.get("infoCount").getAsInt(), hc);
      }
    }
    if (java.has("error-locations")) {
      JsonArray el = java.getAsJsonArray("error-locations");
      Assert.assertEquals( "locations count is not correct", errLocs.size(), el.size());
      for (int i = 0; i < errLocs.size(); i++) {
        Assert.assertEquals("Location should be " + el.get(i).getAsString() + ", but was " + errLocs.get(i), errLocs.get(i), el.get(i).getAsString());
      }
    }
    if (focus.has("output")) {
      focus.remove("output");
    }
    JsonArray vr = new JsonArray();
    java.add("output", vr);
    for (ValidationMessage vm : errors) {
      vr.add(vm.getDisplay());
    }
  }
}