package ch.ahdis.validation;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.FHIRFormatError;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.utilities.TextFile;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.VersionUtilities;
import org.hl7.fhir.utilities.npm.NpmPackage;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.yaml.snakeyaml.Yaml;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.AppProperties.ImplementationGuide;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ch.ahdis.matchbox.util.PackageCacheInitializer;

/**
 * see https://www.baeldung.com/springjunit4classrunner-parameterized read the
 * implementation guides defined in ig and execute the validations
 * 
 * when running the testsuite we have sometime spurious errors during startup and alos during proccessing (Bundles to big?))
 * 
 * @author oliveregger
 */
@RunWith(Parameterized.class)
public class IgValidateR4TestStandalone {

  static private Set<String> loadedIgs = new HashSet<String>();

    private Resource resource;
  private String name;
  
  
  public static List<ImplementationGuide> getImplementationGuides() {
    Yaml yaml = new Yaml();
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream("./with-preload/application.yaml");
    } catch (FileNotFoundException e) {
      return null;
    }
    Map<String, Object> obj = yaml.load(inputStream);
    return PackageCacheInitializer.getIgs(obj);
  }


  @Parameters(name = "{index}: file {0}")
  public static Iterable<Object[]> data() throws ParserConfigurationException, IOException, FHIRFormatError {

    List<ImplementationGuide> igs = getImplementationGuides();
    List<Object[]> objects = new ArrayList<Object[]>();
    for (ImplementationGuide ig : igs) {
      String igName = ig.getName() + "#" + ig.getVersion();
      List<Resource> resources = getResources(igName);
      for (Resource fn : resources) {
        String name = igName + "-" + fn.getResourceType() + "-" + fn.getId();
        objects.add(new Object[] { name, fn });
      }
    }
    return objects;
  }

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IgValidateR4TestStandalone.class);

  static private Map<String, byte[]> fetchByPackage(String src, boolean examples) throws Exception {
    String pkg = src.replace('#', '-');
    String home = System.getProperty("user.home");
    String tgz = home + "/.fhir/package-client/packages.fhir.org-" + pkg + ".tgz";

    InputStream inputStream = new FileInputStream(tgz);
    NpmPackage pi = NpmPackage.fromPackage(inputStream, null, true);
    return loadPackage(pi, examples);
  }

  static public boolean process(String file) {
    if (file == null) {
      return false;
    }
    if ("ig-r4.json".equals(file)) {
      return false;
    }
    if ("package.json".equals(file)) {
      return false;
    }
    if (file.startsWith("ConceptMap-")) {
      return false;
    }
    return true;
  }

  static public Map<String, byte[]> loadPackage(NpmPackage pi, boolean examples) throws Exception {
    Map<String, byte[]> res = new HashMap<String, byte[]>();
    if (!examples) {
      for (String s : pi.dependencies()) {
        if (!loadedIgs.contains(s)) {
          if (!VersionUtilities.isCorePackage(s)) {
            System.out.println("+  .. load IG from " + s);
            res.putAll(fetchByPackage(s, false));
          }
        }
      }
    }

    if (pi != null) {
      if (examples) {
        for (String s : pi.list("example")) {
          if (process(s)) {
            res.put(s, TextFile.streamToBytes(pi.load("example", s)));
          }
        }
      } else {
        for (String s : pi.list("package")) {
          if (process(s)) {
            res.put(s, TextFile.streamToBytes(pi.load("package", s)));
          }
        }
      }
    }
    return res;
  }

  static private boolean exemptFile(String fn) {
    return Utilities.existsInList(fn, "spec.internals", "version.info", "schematron.zip", "package.json");
  }

  static public List<Resource> loadIg(String src, boolean examples) throws IOException, FHIRException, Exception {
    List<Resource> resources = new ArrayList<Resource>();
    try {
      Map<String, byte[]> source = fetchByPackage(src, examples);
      String version = "4.0.1";
      for (Entry<String, byte[]> t : source.entrySet()) {
        String fn = t.getKey();
        if (!exemptFile(fn)) {
          Resource r = loadFileWithErrorChecking(version, t, fn);
          if (r != null) {
            resources.add(r);
          }
        }
      }
    } catch (java.io.FileNotFoundException e) {
      
    }
    return resources;
  }

  static public Resource loadFileWithErrorChecking(String version, Entry<String, byte[]> t, String fn) {
    Resource r = null;
    try {
      r = loadResourceByVersion(version, t.getValue(), fn);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return r;
  }

  static public org.hl7.fhir.r4.model.Resource loadResourceByVersion(String version, byte[] content, String fn)
      throws IOException, Exception {
    org.hl7.fhir.r4.model.Resource r = null;
    if (version.startsWith("4.0")) {
      if (fn.endsWith(".xml") && !fn.endsWith("template.xml"))
        r = new org.hl7.fhir.r4.formats.XmlParser().parse(new ByteArrayInputStream(content));
      else if (fn.endsWith(".json") && !fn.endsWith("template.json"))
        r = new org.hl7.fhir.r4.formats.JsonParser().parse(new ByteArrayInputStream(content));
      else if (fn.endsWith(".txt") || fn.endsWith(".map"))
        r = new org.hl7.fhir.r4.utils.StructureMapUtilities(null).parse(new String(content), fn);
      else
        throw new Exception("Unsupported format for " + fn);
    } else
      throw new Exception("Unsupported version " + version);
    return r;
  }

  static private List<Resource> getResources(String implementationGuide) {
    List<Resource> resources = null;
    try {
      resources = loadIg(implementationGuide, true);
    } catch (Exception e) {
      log.error("error loading R4 or ImplementationGuide", e);
      return null;
    }

    return resources;
  }

  public IgValidateR4TestStandalone(String name, Resource resource) {
    super();
    this.resource = resource;
    this.name = name;
  }

  @Test
  public void validate() throws Exception {
    OperationOutcome outcome = validate(resource);
    int fails = getValidationFailures(outcome);
    if (fails > 0) {
      assertEquals("success", outcome.toString());
    }
  }

  static public int getValidationFailures(OperationOutcome outcome) {
    int fails = 0;
    if (outcome != null && outcome.getIssue() != null) {
      for (OperationOutcomeIssueComponent issue : outcome.getIssue()) {
        if (IssueSeverity.FATAL == issue.getSeverity()) {
          ++fails;
        }
        if (IssueSeverity.ERROR == issue.getSeverity()) {
          ++fails;
        }
      }
    }
    return fails;
  }

  public boolean upload(String implementationGuide, String targetServer) {
    List<Resource> resources = getResources(implementationGuide);
    try {
      for (Resource resource : resources) {
        validate(resource, targetServer);
      }
    } catch (Exception e) {
      log.error("error loading R4 or ImplementationGuide", e);
    }
    return false;
  }

  public OperationOutcome validate(Resource resource) throws IOException {
  	return validate(resource, GenericFhirClient.testServer);
  }


  public OperationOutcome validate(Resource resource, String targetServer) throws IOException {
    log.debug("validating resource" + resource.getId() + "with" + targetServer);
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();

    boolean skip = "ch.fhir.ig.ch-core#1.0.0-PractitionerRole-HPWengerRole".equals(name); // wrong value inside
    skip = skip || "ch.fhir.ig.ch-epr-mhealth#0.1.2-Bundle-2-7-BundleProvideDocument".equals(name); // error in testcase, however cannot reproduce yet directly ???
    if (skip) {
      log.error("ignoring validation for " + name);
      Assume.assumeFalse(skip);
    }

    GenericFhirClient genericClient = new GenericFhirClient(contextR4);

    String content =  new org.hl7.fhir.r4.formats.JsonParser().composeString(resource);
    OperationOutcome outcome = (OperationOutcome) genericClient.validate(content, null);

    if (outcome == null) {
      log.debug(contextR4.newXmlParser().encodeResourceToString(resource));
      log.error("should have a return element");
      log.error(contextR4.newXmlParser().encodeResourceToString(outcome));
    } else {
      if (getValidationFailures(outcome) > 0) {
        log.debug(contextR4.newXmlParser().encodeResourceToString(resource));
        log.debug("Validation Errors " + getValidationFailures(outcome));
        log.error(contextR4.newXmlParser().encodeResourceToString(outcome));
      }
    }

    return outcome;
  }

  public static void main(String[] args) throws Exception {

    System.out.println("Matchbox IgValidateR4");
    if (hasParam(args, "-ig") && hasParam(args, "-target")) {
      String ig = getParam(args, "-ig");
      String target = getParam(args, "-target");
      IgValidateR4TestStandalone igupload = new IgValidateR4TestStandalone(null, null);
      igupload.upload(ig, target);

    } else {
      System.out.println("-ig or -target missing.");
      System.out.println("-ig [package]: an IG or profile definition to load with format package#version");
      System.out.println("-target [url]: taget fhir server");
    }

  }

  private static boolean hasParam(String[] args, String param) {
    for (String a : args)
      if (a.equals(param))
        return true;
    return false;
  }

  private static String getParam(String[] args, String param) {
    for (int i = 0; i < args.length - 1; i++)
      if (args[i].equals(param))
        return args[i + 1];
    return null;
  }

};
