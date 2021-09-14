package ch.ahdis.validation;

import static org.junit.Assert.assertEquals;

import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.Application;

/**
 * see https://www.baeldung.com/springjunit4classrunner-parameterized
 * read the implementation guides defined in ig and execute the validations
 * @author oliveregger
 */
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Application.class})
@RunWith(Parameterized.class)
public class IgValidateR4Test extends IgValidateR4TestStandalone{

  @ClassRule
  public static final SpringClassRule aaa = new SpringClassRule();

  @Rule
  public final SpringMethodRule smr = new SpringMethodRule();
  
  
  private Resource resource;
  private String name;
  
  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IgValidateR4Test.class);

  
  public IgValidateR4Test(String name, Resource resource) {
    super(name, resource);
    this.resource = resource;
    this.name = name;
  }

  @Test
  public void validate() throws Exception {    
    OperationOutcome outcome = validate(resource); 
    int fails = getValidationFailures(outcome);
    if (fails>0) {
      FhirContext contextR4 = FhirVersionEnum.R4.newContext();
      log.error("failing "+this.name);
      log.debug(contextR4.newJsonParser().encodeResourceToString(resource));
      String outcomeError = this.name+":"+contextR4.newJsonParser().encodeResourceToString(outcome);
      log.debug(contextR4.newJsonParser().encodeResourceToString(outcome));
      assertEquals("failed", outcomeError);
    }
  }

};
