package ch.ahdis.validation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.instance.model.api.IPrimitiveType;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.context.RuntimeResourceDefinition;
import ca.uhn.fhir.fhirpath.IFhirPath;
import ch.ahdis.fhir.hapi.jpa.validation.Canonicalizer;


// Strange, works only with JUNIT4, why?
public class CanonicalizerTests {
  
  @Test
  public void elementsInJsonOrderAsXml() {
    
    String contentString = "";
    try {
      contentString = this.getContent("canonicalize-elements.json");
    } catch(Exception e) {
      assertTrue("catched exception", false);
    }
    
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();

    IBaseResource resource = contextR4.newJsonParser().parseResource(contentString);
    Canonicalizer canonicalizer= new Canonicalizer(contextR4);
    IBaseResource result =  canonicalizer.canonicalize(resource);
    
    String resultString = contextR4.newJsonParser().encodeResourceToString(result);
    
    assertEquals(resultString.indexOf("\"id\""), -1);
    assertThat("identifier",
        resultString.indexOf("identifier"),
        greaterThan(resultString.indexOf("resourceType")));
    assertThat("name",
        resultString.indexOf("name"),
        greaterThan(resultString.indexOf("identifier")));
    assertThat("address",
        resultString.indexOf("address"),
        greaterThan(resultString.indexOf("birthDate")));
  }
  
  @Test
  public void patient() {
    
    String contentString = "";
    try {
      contentString = this.getContent("canonicalize-patient.json");
    } catch(Exception e) {
      assertTrue("catched exception", false);
    }
    
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    IBaseResource resource = contextR4.newJsonParser().parseResource(contentString);
    
    Canonicalizer canonicalizer= new Canonicalizer(contextR4);
    IBaseResource result =  canonicalizer.canonicalize(resource);
    IFhirPath fhirPath = contextR4.newFhirPath();
    

    assertEquals("http://hl7.org/fhir/StructureDefinition/patient-birthPlace", fhirPath.evaluateFirst(result, "extension[0].url", IPrimitiveType.class).get().getValueAsString());
    assertEquals("http://hl7.org/fhir/StructureDefinition/patient-citizenship", fhirPath.evaluateFirst(result, "extension[1].url", IPrimitiveType.class).get().getValueAsString());
    assertEquals("code", fhirPath.evaluateFirst(result, "extension[1].extension[0].url", IPrimitiveType.class).get().getValueAsString());
    assertEquals("period", fhirPath.evaluateFirst(result, "extension[1].extension[1].url", IPrimitiveType.class).get().getValueAsString());
    
  }
  
  @Test
  public void chargeItem() {
    
    String contentString = "";
    try {
      contentString = this.getContent("canonicalize-chargeitem.json");
    } catch(Exception e) {
      assertTrue("catched exception", false);
    }
    
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    IBaseResource resource = contextR4.newJsonParser().parseResource(contentString);
    
    Canonicalizer canonicalizer= new Canonicalizer(contextR4);
    IBaseResource result =  canonicalizer.canonicalize(resource);
    IFhirPath fhirPath = contextR4.newFhirPath();

  }


  
  private static BaseRuntimeChildDefinition getBaseRuntimeChildDefinition(FhirContext theFhirContext, String theFieldName, IBaseResource theFrom) {
    RuntimeResourceDefinition definition = theFhirContext.getResourceDefinition(theFrom);
    BaseRuntimeChildDefinition childDefinition = definition.getChildByName(theFieldName);
    Validate.notNull(childDefinition);
    return childDefinition;
  }

  public static void clearField(FhirContext theFhirContext, IBaseResource theResource, String theFieldName) {
    BaseRuntimeChildDefinition childDefinition = getBaseRuntimeChildDefinition(theFhirContext, theFieldName, theResource);
    childDefinition.getMutator().setValue(theResource, null);
  }

  
  private String getContent(String resourceName) throws IOException {
    Resource resource = new ClassPathResource(resourceName);
    File file = resource.getFile();
    return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
  }

}
