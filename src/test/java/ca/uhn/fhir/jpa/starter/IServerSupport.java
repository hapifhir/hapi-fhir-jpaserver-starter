package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

public interface  IServerSupport {

//  default void putResourceDstu3(String resourceFileName, String id, FhirContext theCtx, IGenericClient theClient) {
//    InputStream is = ExampleServerDstu3IT.class.getResourceAsStream(resourceFileName);
//    Scanner scanner = new Scanner(is).useDelimiter("\\A");
//    String json = scanner.hasNext() ? scanner.next() : "";
//
//    boolean isJson = resourceFileName.endsWith("json");
//
//    IBaseResource resource = isJson ? theCtx.newJsonParser().parseResource(json) : theCtx.newXmlParser().parseResource(json);
//
//    if (resource instanceof Bundle) {
//      theClient.transaction().withBundle((Bundle) resource).execute();
//    }
//    else {
//      theClient.update().resource(resource).withId(id).execute();
//    }
//  }

  default String stringFromResource(String theLocation) throws IOException {
    InputStream is = null;
    if (theLocation.startsWith(File.separator)) {
      is = new FileInputStream(theLocation);
    } else {
      DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
      Resource resource = resourceLoader.getResource(theLocation);
      is = resource.getInputStream();
    }
    return IOUtils.toString(is, Charsets.UTF_8);
  }
}
