package ch.ahdis.validation;

/**
 * Attention: if it is the first test run, an error about not connecting to port 8080 appears, running IgValidateR4 first works 
 */
import static org.junit.Assert.assertTrue;

import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.Application;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Application.class})
public class IgValidateRawProfileTest {
  
  
  private String targetServer = "http://localhost:8080/hapi-fhir-jpavalidator/fhir";
  
  @Test
  public void validateRaw() {
    FhirContext contextR4 = FhirVersionEnum.R4.newContext();
    ValidationClient validationClient = new ValidationClient(contextR4, this.targetServer);
    String patient = "<Patient xmlns=\"http://hl7.org/fhir\">\n" + 
        "            <id value=\"example\"/>\n" + 
        "            <text>\n" + 
        "               <status value=\"generated\"/>\n" + 
        "               <div xmlns=\"http://www.w3.org/1999/xhtml\">42 </div>\n" + 
        "            </text>\n" + 
        "         </Patient>\n";

    validationClient.capabilities();
    IBaseOperationOutcome operationOutcome = validationClient.validate(patient, "http://hl7.org/fhir/StructureDefinition/Patient");
    assertTrue(operationOutcome!=null);
  }

}
