package ch.ahdis.matchbox.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.utilities.Utilities;
import org.hl7.fhir.utilities.npm.FilesystemPackageCacheManager;
import org.hl7.fhir.utilities.npm.ToolsVersion;
import org.yaml.snakeyaml.Yaml;

import ca.uhn.fhir.jpa.packages.PackageInstallationSpec;
import ca.uhn.fhir.jpa.starter.AppProperties.ImplementationGuide;

public class PackageCacheInitializer {

  private FilesystemPackageCacheManager pcm = null;

  PackageCacheInitializer() {

  }

  private InputStream fetchFromUrlSpecific(String source) throws FHIRException {
    try {
      URL url = new URL(source);
      URLConnection c = url.openConnection();
      return c.getInputStream();
    } catch (Exception e) {
      throw new FHIRException(e.getMessage(), e);
    }
  }

  public void pkg(String id, String version, String tgz, String desc) throws IOException, FHIRException {
    pcm = new FilesystemPackageCacheManager.Builder().build();
    if (tgz != null) {
      InputStream inputStream = null;
      if (Utilities.isURL(tgz)) {
        inputStream = fetchFromUrlSpecific(tgz);
      } else {
        inputStream = new FileInputStream(tgz);
      }
      pcm.addPackageToCache(id, version, inputStream, desc);
      System.out.println("added package " + id + " version " + version);
    } else {
      pcm.loadPackage(id, version);
      System.out.println("loaded package " + id + " version " + version);
    }
  }
  

  public static List<ImplementationGuide> getImplementationGuides(boolean noSystems) {
    Yaml yaml = new Yaml();
    PackageCacheInitializer pci = new PackageCacheInitializer();
    InputStream inputStream = yaml.getClass().getClassLoader().getResourceAsStream("application.yaml");
    Map<String, Object> obj = yaml.load(inputStream);
    List<ImplementationGuide> igs = getIgs(obj, noSystems);
    return igs;
  }

  /**
   * not very nice, however i don't know another way since spring is not yet
   * available for injection laoding directly application.yml from
   * https://www.baeldung.com/java-snake-yaml
   * 
   * @param map
   * @return
   */
  public static List<ImplementationGuide> getIgs(Map<String, Object> map, boolean noSystems) {
    Map<String, Object> hapi = (Map<String, Object>) map.get("hapi");
    if (hapi != null) {
      Map<String, Object> fhir = (Map<String, Object>) hapi.get("fhir");
      if (hapi != null) {
        LinkedHashMap<String, Map<String, String>> igs =  (LinkedHashMap<String, Map<String, String>>) fhir.get("implementationguides");
        if (igs != null) {
          ArrayList<ImplementationGuide> igProperties = new ArrayList<ImplementationGuide>();
          for (String igKey : igs.keySet()) {
            Map<String, String> ig = igs.get(igKey);
            ImplementationGuide igProperty = new ImplementationGuide();
            igProperty.setName(ig.get("name"));
            igProperty.setVersion(ig.get("version"));
            igProperty.setUrl(ig.get("url"));
            if (!noSystems || !(igProperty.getName().equals("hl7.fhir.r4.core") || igProperty.getName().equals("hl7.terminology"))) {
              igProperties.add(igProperty);
            }
          }
          return igProperties;
        }
      }
    }
    return null;
  }

  public static void main(String[] args) {

    PackageCacheInitializer pci = new PackageCacheInitializer();
    if (hasParam(args, "-id") & hasParam(args, "-v")) {
      String id = getParam(args, "-id");
      String version = getParam(args, "-v");
      String tgz = getParam(args, "-tgz");
      String desc = getParam(args, "-desc");
      try {
        pci.pkg(id, version, tgz, desc);
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(-1);
      } catch (FHIRException e) {
        e.printStackTrace();
        System.exit(-1);
      }
    }
    if (hasParam(args, "-auto")) {
      List<ImplementationGuide> igs = getImplementationGuides(true);
      
      for (ImplementationGuide ig : igs) {
        PackageInstallationSpec spec = new PackageInstallationSpec()
            .setPackageUrl(ig.getUrl())
            .setName(ig.getName())
            .setVersion(ig.getVersion())
            .setInstallMode(PackageInstallationSpec.InstallModeEnum.STORE_ONLY);
        try {
          pci.pkg(ig.getName(), ig.getVersion(), ig.getUrl(), null);
        } catch (IOException e) {
          e.printStackTrace();
          System.exit(-1);
        } catch (FHIRException e) {
          e.printStackTrace();
          System.exit(-1);
        }
      }
    } else {
      System.out.println("-id package id");
      System.out.println("-v version");
      System.out.println("-tgz path to package if verison is dev");
      System.out.println("-auto if properites should be read of application.yaml");
      System.exit(-1);
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

}
