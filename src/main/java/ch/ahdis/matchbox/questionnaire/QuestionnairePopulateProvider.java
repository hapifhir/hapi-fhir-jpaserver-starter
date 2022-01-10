package ch.ahdis.matchbox.questionnaire;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.convertors.factory.VersionConvertorFactory_40_50;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.DomainResource;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Questionnaire;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Type;
import org.hl7.fhir.r5.context.IWorkerContext;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.elementmodel.Manager.FhirFormat;
import org.hl7.fhir.r5.formats.IParser.OutputStyle;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.StructureDefinition;
import org.hl7.fhir.r5.model.StructureMap.StructureMapStructureComponent;
import org.hl7.fhir.r5.utils.FHIRPathEngine;
import org.hl7.fhir.r5.utils.structuremap.StructureMapUtilities;
import org.springframework.beans.factory.annotation.Autowired;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ch.ahdis.matchbox.mappinglanguage.ConvertingWorkerContext;
import ch.ahdis.matchbox.mappinglanguage.ElementModelSorter;
import ch.ahdis.matchbox.mappinglanguage.TransformSupportServices;

/**
 * $populate operation for Questionnaire Resource
 *
 */
public class QuestionnairePopulateProvider {

	public final static String LAUNCH_CONTEXT = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-launchContext";
	public final static String SOURCE_QUERIES = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-sourceQueries";
	public final static String SOURCE_STRUCTURE_MAP = "http://hl7.org/fhir/uv/sdc/StructureDefinition/sdc-questionnaire-sourceStructureMap";
	
	private String baseUrl;
	
	
	@Autowired
	protected ConvertingWorkerContext baseWorkerContext;
	
	
	
  /*public void updateWorkerContext(Questionnaire theResource) {
    org.hl7.fhir.r5.model.Questionnaire cached = fhirContext.fetchResource(org.hl7.fhir.r5.model.Questionnaire.class, theResource.getUrl());
    if (cached != null) {
      fhirContext.dropResource(cached);
    }    
    org.hl7.fhir.r5.model.Questionnaire r5Structure = (org.hl7.fhir.r5.model.Questionnaire) VersionConvertor_40_50.convertResource(theResource);
    fhirContext.cacheResource(r5Structure);
  }*/
  
  /*public org.hl7.fhir.r5.model.Questionnaire getByUrl(String url) {
  return fhirContext.fetchResource(org.hl7.fhir.r5.model.Questionnaire.class, url);
  }*/
	
	@Operation(name = "$populate", type = Questionnaire.class, idempotent = true)
	public QuestionnaireResponse extract( 
			@OperationParam(name = "questionnaire", min = 0, max = 1) Questionnaire questionnaire,
			@OperationParam(name = "questionnaireRef", min = 0, max = 1) Reference questionnaireRef,
			@OperationParam(name = "subject", min = 1, max = 1, type=Reference.class) Reference ref,
			
			@OperationParam(name = "identifier", min=0, max=1) Identifier identifier,
			@OperationParam(name = "launchContext", min=0, max=1) Parameters launchContext,
			@OperationParam(name = "canonical", min=0, max=1) String canonical,
			@OperationParam(name = "subject", min=0, max=1) Reference subject,
			@OperationParam(name = "content", min=0 ) List<Reference> content,
			@OperationParam(name = "local", min=0, max=1 ) BooleanType local)
	      throws IOException {		
		  
		  StructureMapUtilities utils = new StructureMapUtilities(baseWorkerContext, new TransformSupportServices(baseWorkerContext, new ArrayList<Base>()));
			
		  if (questionnaire==null && questionnaireRef==null) throw new UnprocessableEntityException("No questionnaire given in Parameters");
		  
		  // resolve questionnaire reference if given
		  if (questionnaire==null && questionnaireRef!=null) {
			  //workerContext.load(org.hl7.fhir.r4.model.Questionnaire.class, questionnaireRef.getReference());
			  org.hl7.fhir.r5.model.Questionnaire questionnaireR5 = baseWorkerContext.fetchResource(org.hl7.fhir.r5.model.Questionnaire.class, questionnaireRef.getReference());
			  if (questionnaireR5==null) throw new UnprocessableEntityException("Questionnaire referenced by questionnaireRef could not be resolved.");
			  questionnaire = (Questionnaire) VersionConvertorFactory_40_50.convertResource(questionnaireR5);
		  }
		  
		  // convert questionaire to element model		      		    
	      org.hl7.fhir.r5.elementmodel.Element src = convertToElementModel(baseWorkerContext, questionnaire);
	      
	      // get launch context from questionnaire
	      org.hl7.fhir.r5.elementmodel.Element launchContextDefinition = src.getExtension(LAUNCH_CONTEXT);
	      if (launchContextDefinition == null) throw new UnprocessableEntityException("No sdc-questionnaire-launchContext extension found in resource");
	      
   	      // get structure map from questionnaire
	      Base mapUrlValue = src.getExtensionValue(SOURCE_STRUCTURE_MAP);
	      if (mapUrlValue == null) throw new UnprocessableEntityException("No sdc-questionnaire-sourceStructureMap extension found in resource");
	      String mapUrl = mapUrlValue.primitiveValue();
	      //workerContext.loadMap(mapUrl);
	      org.hl7.fhir.r5.model.StructureMap map = baseWorkerContext.getTransform(mapUrl);
	      if (map == null) {
	          throw new UnprocessableEntityException("Map not available with canonical url "+mapUrl);
	      }
	      
	      org.hl7.fhir.r5.elementmodel.Element bundle = null; 	      // input for structure map; initialized later
	      
	      // get source queries from questionnaire
	      Extension sourceQueriesExt = questionnaire.getExtensionByUrl(SOURCE_QUERIES);
	      
	      // experimental branch with launch context and no source queries bundle
	      if (launchContext != null && sourceQueriesExt == null) {
	    	  // convert launch context to element model and use as input for structure map		    
	        Bundle prepopBundle = new Bundle();
	        prepopBundle.addEntry().setResource(launchContext);
	        bundle = convertToElementModel(baseWorkerContext, prepopBundle);	
	      } else {
	    	  // normal branch with source queries
		      if (sourceQueriesExt == null) throw new UnprocessableEntityException("No sdc-questionnaire-sourceQueries extension found in resource");
			  Type t = sourceQueriesExt.getValue();
			  if (! (t instanceof Reference)) throw new UnprocessableEntityException("sdc-questionnaire-sourceQueries must have reference");
			  Resource sourceQueriesBundleResource = resolveResource(questionnaire, (Reference) t);
			  if (sourceQueriesBundleResource == null) throw new UnprocessableEntityException("sdc-questionnaire-sourceQueries not resolved");
			  if (! (sourceQueriesBundleResource instanceof Bundle)) throw new UnprocessableEntityException("sdc-questionnaire-sourceQueries is not a bundle");
			  Bundle sourceQueriesBundle = (Bundle) sourceQueriesBundleResource;
		      		  	      	   	     
		      // build input bundle for structure map
		      Bundle processBundle = new Bundle();
		      for (Bundle.BundleEntryComponent entry : sourceQueriesBundle.getEntry()) {
		    	  if (entry.hasResource()) {
		    		  if (entry.getResource() instanceof Bundle) {
		    			processBundle.addEntry().setResource(entry.getResource()).setFullUrl(entry.getFullUrl());  
		    		  } else {
		    		    processBundle.addEntry().setResource(wrapIntoBundle(entry.getResource())).setFullUrl(entry.getFullUrl());
		    		  }
		    	  } else if (entry.hasRequest()) {
		    		  if (entry.getRequest().getMethod()!=Bundle.HTTPVerb.GET) throw new UnprocessableEntityException("Bundle request method must be GET");
		    		  String url = entry.getRequest().getUrl();
		    		  url = evaluateFhirPath(baseWorkerContext, subject, url);
		    		  Bundle result = resolveBundleFromUri(url);
		    		  processBundle.addEntry().setResource(result).setFullUrl(entry.getFullUrl());
		    	  }
		      }
		      
		      // convert input bundle to element model		           
		      bundle = convertToElementModel(baseWorkerContext, processBundle);	
	      }
	      
	      // build output QuestionnaireResponse using element model
	      org.hl7.fhir.r5.elementmodel.Element r = getTargetResourceFromStructureMap(baseWorkerContext, map);
	      if (r == null) {
	        throw new UnprocessableEntityException("Target Structure can not be resolved from map, is the corresponding implmentation guide provided?");
	      }	      
	      utils.transform(null, bundle, map, r);
	      ElementModelSorter.sort(r);
	      
	      // convert output to R4 resource
	      IBaseResource result = convertToR4(baseWorkerContext, r);	      
	      if (!(result instanceof QuestionnaireResponse)) throw new UnprocessableEntityException("Structure Map does not produce correct output resource type.");	      
	      QuestionnaireResponse questionnaireResponse = (QuestionnaireResponse) result;
	      
	      // add subject and identifier to response if given	      
	      if (subject != null) questionnaireResponse.setSubject(subject);
	      if (identifier != null) questionnaireResponse.setIdentifier(identifier);
	      
	      return questionnaireResponse;
	      		     		
	}
	  
    /**
     * create target resource from structure map
     * @param map
     * @return
     */
	private org.hl7.fhir.r5.elementmodel.Element getTargetResourceFromStructureMap(IWorkerContext workerContext, org.hl7.fhir.r5.model.StructureMap map) {
	    String targetTypeUrl = null;
	    for (StructureMapStructureComponent component : map.getStructure()) {
	      if (component.getMode() == org.hl7.fhir.r5.model.StructureMap.StructureMapModelMode.TARGET) {
	        targetTypeUrl = component.getUrl();
	        break;
	      }
	    }
	
	    if (targetTypeUrl == null)
	      throw new FHIRException("Unable to determine resource URL for target type");
	
	    StructureDefinition structureDefinition = workerContext.fetchResource(StructureDefinition.class, targetTypeUrl);

	    if (structureDefinition == null)
	      throw new FHIRException("Unable to determine StructureDefinition for target type");
	
	    return Manager.build(baseWorkerContext, structureDefinition);
	  }
	
	/**
	 * convert R4 resources to element model
	 * @param inputResource
	 * @return
	 */
	private org.hl7.fhir.r5.elementmodel.Element convertToElementModel(IWorkerContext workerContext, Resource inputResource) {
		 String inStr = FhirContext.forR4Cached().newJsonParser().encodeResourceToString(inputResource);
		 
		 try {
	       return Manager.parseSingle(workerContext, new ByteArrayInputStream(inStr.getBytes()), FhirFormat.JSON);
		 } catch (IOException e) {
			 throw new UnprocessableEntityException("Cannot convert resource to element model");
		 }	 
	}
	
	/**
	 * convert from element model to R4 resource
	 * @param input
	 * @return
	 */
	private IBaseResource convertToR4(IWorkerContext workerContext, org.hl7.fhir.r5.elementmodel.Element input) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
		  new org.hl7.fhir.r5.elementmodel.JsonParser(workerContext).compose(input, output, OutputStyle.NORMAL, null);
		  return FhirContext.forR4Cached().newJsonParser().parseResource(new String(output.toByteArray()));
		} catch (IOException e) {
			throw new UnprocessableEntityException("Cannot convert to R4");
		}
	}
	
	/**
	 * locally resolve reference
	 * @param container
	 * @param reference
	 * @return
	 */
	private Resource resolveResource(DomainResource container, Reference reference) {
		if (reference.hasReference()) {
			String targetRef = reference.getReference();		
			List<Resource> resources = container.getContained();		
			for (Resource resource : resources) {			
				if (targetRef.equals(resource.getId())) {
					return resource;
				}
			}
		}
		return null;
	}
	
	/**
	 * wrap single resource into bundle
	 * @param resource
	 * @return
	 */
	private Bundle wrapIntoBundle(Resource resource) {
		Bundle result = new Bundle();
		result.setType(BundleType.BATCHRESPONSE);
		result.addEntry().setResource(resource);
		return result;
	}
	
	/**
	 * resolve bundle from uri (search)
	 * @param uri
	 * @return
	 */
	private Bundle resolveBundleFromUri(String uri) {
		if (baseUrl==null) throw new UnprocessableEntityException("missing baseUrl");
		System.out.println("fetch from external: "+uri);
		IGenericClient client = FhirContext.forR4Cached().newRestfulGenericClient(baseUrl);
		Bundle result = client.search()
			.byUrl(uri)
			.returnBundle(Bundle.class)
			.execute();
		System.out.println("retrieved entries: "+result.getEntry().size());
		return result;
	}
	
	/**
	 * replace FHIR path expressions in uri
	 * @param uri
	 * @return
	 */
	private String evaluateFhirPath(IWorkerContext workerContext, Reference subject, String uri) {
		
		while (uri.indexOf("{{") >= 0) {
			int p = uri.indexOf("{{");
			int c = uri.indexOf("}}");
			String expression = uri.substring(p+2,c);
		
			String r = null;
			if (expression.equals("%LaunchPatient.id")) {
				r = subject.getReference();
			} else {
				// TODO what is the rool element?
				System.out.println("IN:"+expression);
				FHIRPathEngine fp = new FHIRPathEngine(workerContext);			
				r = fp.evaluateToString(null, expression);
				System.out.println("OUT:"+r);
			}
			
			uri = uri.substring(0,p)+r+uri.substring(c+2);
		}
				
		return uri;
	}
	
	//FhirPathR5 fhirPath = new FhirPathR5(FhirContext.forR4());
    //fhirPath.evaluate(theInput, thePath, theReturnType)
}
