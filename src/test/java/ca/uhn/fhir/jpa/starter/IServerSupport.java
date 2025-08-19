package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.api.dao.IFhirResourceDao;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public interface  IServerSupport {

  default IBaseResource loadResource(String theLocation, FhirContext theFhirContext, DaoRegistry theDaoRegistry) throws IOException {
    String json = stringFromResource(theLocation);
    IBaseResource resource = theFhirContext.newJsonParser().parseResource(json);
    IFhirResourceDao<IBaseResource> dao = theDaoRegistry.getResourceDao(resource.getIdElement().getResourceType());
    if (dao == null) {
      return null;
    } else {
      dao.update(resource);
      return resource;
    }
  }

  default IBaseBundle loadBundle(String theLocation, FhirContext theFhirContext, IGenericClient theClient) throws IOException {
	  String json = stringFromResource(theLocation);
	  IBaseBundle bundle = (IBaseBundle) theFhirContext.newJsonParser().parseResource(json);
	  return theClient.transaction().withBundle(bundle).execute();
  }

  default String stringFromResource(String theLocation) throws IOException {
    InputStream is = null;
    if (theLocation.startsWith(File.separator)) {
      is = new FileInputStream(theLocation);
    } else {
      DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
      Resource resource = resourceLoader.getResource(theLocation);
      is = resource.getInputStream();
    }
    return IOUtils.toString(is, StandardCharsets.UTF_8);
  }
}
