package ch.ahdis.validation;

import static org.junit.Assert.assertTrue;

import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;


public class IgValidateProfileTest {
  
  
  private String targetServer = "http://test.ahdis.ch/hapi-fhir-jpavalidator/fhir";

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
    IBaseOperationOutcome operationOutcome = validationClient.validate(patient, "http://hl7.org/fhir/StructureDefinition/Patient");
    assertTrue(operationOutcome!=null);
  }

}
