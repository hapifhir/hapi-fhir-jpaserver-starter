package ch.ahdis.validation;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;

import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.starter.Application;
import ca.uhn.fhir.rest.api.EncodingEnum;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Application.class})
public class QrTransformTests {
  
  private FhirContext contextR4 = FhirVersionEnum.R4.newContext();
  private GenericFhirClient genericClient = new GenericFhirClient(contextR4);


//  @ClassRule
//  public static final SpringClassRule scr = new SpringClassRule();

//  @Rule
//  public final SpringMethodRule smr = new SpringMethodRule();

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QrTransformTests.class);

  
  @Before 
  public void setup() {
    contextR4 = FhirVersionEnum.R4.newContext();
    genericClient = new GenericFhirClient(contextR4);
  }

  
  private QuestionnaireResponse convertQrToBundleAndBack(QuestionnaireResponse qr, String mapToBundle, String mapToQr) {
    String bundle = genericClient.convert(qr, EncodingEnum.XML ,mapToBundle, "application/xml+fhir");
    assertNotNull(bundle);
    QuestionnaireResponse bundleReceived = (QuestionnaireResponse) genericClient.convert(bundle, EncodingEnum.XML, mapToQr, "application/json+fhir");
    assertNotNull(bundleReceived);
    System.out.println(contextR4.newXmlParser().encodeResourceToString(bundleReceived));
    return bundleReceived;
  }  



  public void convertOrfQuestionnaireResponse(String url) throws IOException {
    InputStream inputStream = new URL(url).openStream();
    QuestionnaireResponse qr = (QuestionnaireResponse) contextR4.newJsonParser().parseResource(inputStream);
    assertNotNull(qr);
    QuestionnaireResponse bundleReceived = convertQrToBundleAndBack(qr, "http://fhir.ch/ig/ch-orf/StructureMap/OrfQrToBundle", "http://fhir.ch/ig/ch-orf/StructureMap/OrfBundleToQr");
    compare(qr, bundleReceived, false);
 }

  @Test
  public void convertQuestionnaireResponseOrderReferralFormAndBack() throws IOException {
    convertOrfQuestionnaireResponse("http://build.fhir.org/ig/hl7ch/ch-orf/QuestionnaireResponse-qr-order-referral-form.xml");
  }
  
  public String fshy(String text, Map<String, Object> map) {
    String result = text + "\n";
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      result += entry.getKey() + " = " + entry.getValue() + "\n";
    }
    result += "\n";
    return result;
  }
  
  public String fshyDifferrence(String text, Map<String, ValueDifference<Object>> map) {
    String result = text + "\n";
    for (Map.Entry<String, ValueDifference<Object>> entry : map.entrySet()) {
      result += entry.getKey() + " = " + entry.getValue() + "\n";
    }
    result += "\n";
    return result;
  }

  
  public void compare(IBaseResource left, IBaseResource right, boolean onlyDiffering) {
    
    
    Map<String, String> bundleLeftIds = null;
    Map<String, String> bundleRightIds = null;
    
    String jsonLeft = contextR4.newJsonParser().encodeResourceToString(left);
    String jsonRight = contextR4.newJsonParser().encodeResourceToString(right);
    Gson g = new Gson();
    
    Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
    
    Map<String, Object> leftMap = g.fromJson(jsonLeft, mapType);
    Map<String, Object> rightMap = g.fromJson(jsonRight, mapType);
    
    Map<String, Object> leftFlatMap = FlatMapUtil.flatten(leftMap, bundleLeftIds);
    Map<String, Object> rightFlatMap = FlatMapUtil.flatten(rightMap, bundleRightIds);
    
    
    
    
    log.debug(fshy("resulting transform",rightFlatMap));
    
    MapDifference<String, Object> difference = Maps.difference(leftFlatMap, rightFlatMap);
    if (!difference.areEqual()) {
      log.error(difference.toString());
      log.error(fshy("entries only on left",difference.entriesOnlyOnLeft()));
      log.error(fshy("entries only on right",difference.entriesOnlyOnRight()));
      log.error(fshyDifferrence("entries differing",difference.entriesDiffering()));
    }

    assertTrue(onlyDiffering ? difference.entriesDiffering().isEmpty() : difference.areEqual());

  }

}
