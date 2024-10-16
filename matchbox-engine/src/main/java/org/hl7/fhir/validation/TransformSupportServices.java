package org.hl7.fhir.validation;

import java.io.PrintWriter;
import java.util.List;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r5.conformance.profile.ProfileUtilities;
import org.hl7.fhir.r5.context.SimpleWorkerContext;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.terminologies.ConceptMapEngine;
import org.hl7.fhir.r5.utils.structuremap.ITransformerServices;

public class TransformSupportServices implements ITransformerServices {

  private final PrintWriter mapLog;
  private final SimpleWorkerContext context;
  private List<Base> outputs;

  public TransformSupportServices(List<Base> outputs,
                                  PrintWriter mapLog,
                                  SimpleWorkerContext context) {
    this.outputs = outputs;
    this.mapLog = mapLog;
    this.context = context;
  }

  @Override
  public void log(String message) {
    if (mapLog != null)
      mapLog.println(message);
    System.out.println(message);
  }

// matchbox patch https://github.com/ahdis/matchbox/issues/264
  @Override
  public Base createType(Object appInfo, String name, ProfileUtilities profileUtilities) throws FHIRException {
    StructureDefinition sd = context.fetchResource(StructureDefinition.class, name);
    return Manager.build(context, sd, profileUtilities);
  }

  @Override
  public Base createResource(Object appInfo, Base res, boolean atRootofTransform) {
    if (atRootofTransform)
      outputs.add(res);
    return res;
  }

  @Override
  public Coding translate(Object appInfo, Coding source, String conceptMapUrl) throws FHIRException {
    ConceptMapEngine cme = new ConceptMapEngine(context);
    return cme.translate(source, conceptMapUrl);
  }

  @Override
  public Base resolveReference(Object appContext, String url) throws FHIRException {
    throw new FHIRException("resolveReference is not supported yet");
  }

  @Override
  public List<Base> performSearch(Object appContext, String url) throws FHIRException {
    throw new FHIRException("performSearch is not supported yet");
  }
}
