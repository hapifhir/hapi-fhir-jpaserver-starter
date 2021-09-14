package ch.ahdis.matchbox.mappinglanguage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.utils.structuremap.ITransformerServices;

import ca.uhn.fhir.context.FhirContext;

public class TransformSupportServices implements ITransformerServices {

  private List<Base> outputs;
  private ConvertingWorkerContext fhirContext;
  protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TransformSupportServices.class);

  public TransformSupportServices(ConvertingWorkerContext fhirContext, List<Base> outputs) {
    this.fhirContext = fhirContext;
    this.outputs = outputs;
  }

  @Override
  public Base createType(Object appInfo, String name) throws FHIRException {
    StructureDefinition sd = fhirContext.fetchResource(StructureDefinition.class, name);
    return Manager.build(fhirContext, sd);
  }

  @Override
  public Base createResource(Object appInfo, Base res, boolean atRootofTransform) {
    if (atRootofTransform)
      outputs.add(res);
    return res;
  }

  @Override
  public Coding translate(Object appInfo, Coding source, String conceptMapUrl) throws FHIRException {
    ConceptMapEngine cme = new ConceptMapEngine(fhirContext);
    return cme.translate(source, conceptMapUrl);
  }

  @Override
  public Base resolveReference(Object appContext, String url) throws FHIRException {	
	org.hl7.fhir.r4.model.Resource resource = fhirContext.fetchResourceAsR4(org.hl7.fhir.r4.model.Resource.class, url);
    if (resource != null) {
      String inStr = FhirContext.forR4Cached().newJsonParser().encodeResourceToString(resource);
      try {
        return Manager.parse(fhirContext, new ByteArrayInputStream(inStr.getBytes()), FhirFormat.JSON);
      } catch (IOException e) {
        throw new FHIRException("Cannot convert resource to element model");
      }
    }
    throw new FHIRException("resolveReference, url not found: " + url);
  }

  @Override
  public List<Base> performSearch(Object appContext, String url) throws FHIRException {
    throw new FHIRException("performSearch is not supported yet");
  }

  @Override
  public void log(String message) {
    log.debug(message);
  }

}