package org.hl7.fhir.r5.utils.structuremap;

import org.apache.commons.lang3.NotImplementedException;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.exceptions.PathEngineException;
import org.hl7.fhir.r5.elementmodel.Element;
import org.hl7.fhir.r5.elementmodel.Manager;
import org.hl7.fhir.r5.model.Base;
import org.hl7.fhir.r5.model.Resource;
import org.hl7.fhir.r5.model.ValueSet;
import org.hl7.fhir.utilities.validation.ValidationMessage;
import org.hl7.fhir.validation.instance.InstanceValidator;
import org.hl7.fhir.r5.fhirpath.FHIRPathEngine;
import org.hl7.fhir.r5.fhirpath.FHIRPathUtilityClasses.FunctionDetails;
import org.hl7.fhir.r5.fhirpath.TypeDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FFHIRPathHostServices implements FHIRPathEngine.IEvaluationContext {

	private final StructureMapUtilities structureMapUtilities;

	public FFHIRPathHostServices(StructureMapUtilities structureMapUtilities) {
		this.structureMapUtilities = structureMapUtilities;
	}

	@Override
	public List<Base> resolveConstant(FHIRPathEngine engine, Object appContext, String name, boolean beforeContext,
			boolean explicitConstant) throws PathEngineException {
		Variables vars = (Variables) appContext;
		Base res = vars.get(VariableMode.INPUT, name);
		if (res == null)
			res = vars.get(VariableMode.OUTPUT, name);
		List<Base> result = new ArrayList<Base>();
		if (res != null)
			result.add(res);
		return result;
	}

	@Override
	public TypeDetails resolveConstantType(FHIRPathEngine engine, Object appContext, String name,
			boolean explicitConstant) throws PathEngineException {
		if (!(appContext instanceof VariablesForProfiling))
			throw new Error("Internal Logic Error (wrong type '" + appContext.getClass().getName() + "' in resolveConstantType)");
		VariablesForProfiling vars = (VariablesForProfiling) appContext;
		VariableForProfiling v = vars.get(null, name);
		if (v == null)
			throw new PathEngineException("Unknown variable '" + name + "' from variables " + vars.summary());
		return v.getProperty().getTypes();
	}

	@Override
	public boolean log(String argument, List<Base> focus) {
		throw new Error("Not Implemented Yet");
	}

	@Override
	public FunctionDetails resolveFunction(FHIRPathEngine engine, String functionName) {
		throw new UnsupportedOperationException("Unimplemented method 'resolveFunction'");
	}

	@Override
	public TypeDetails checkFunction(FHIRPathEngine engine, Object appContext, String functionName, TypeDetails focus,
			List<TypeDetails> parameters) throws PathEngineException {
		throw new UnsupportedOperationException("Unimplemented method 'checkFunction'");
	}

	@Override
	public List<Base> executeFunction(FHIRPathEngine engine, Object appContext, List<Base> focus, String functionName,
			List<List<Base>> parameters) {
		throw new UnsupportedOperationException("Unimplemented method 'executeFunction'");
	}

	
	@Override
	public Base resolveReference(FHIRPathEngine engine, Object appContext, String url, Base refContext)
			throws FHIRException {
		if (structureMapUtilities.getServices() == null)
			return null;
		return structureMapUtilities.getServices().resolveReference(appContext, url);
	}

	private boolean noErrorValidationMessages(List<ValidationMessage> valerrors) {
		boolean ok = true;
		for (ValidationMessage v : valerrors)
			ok = ok && !v.getLevel().isError();
		return ok;
	}

	@Override
	public boolean conformsToProfile(FHIRPathEngine engine, Object appContext, Base item, String url)
			throws FHIRException {
		//IResourceValidator val = structureMapUtilities.getWorker().newValidator();
		// we need the same conformToProfile context as we have in the matchbox engine
		// matchbox 3.1.0
		 InstanceValidator val = null;
		 try {
			val = ((ch.ahdis.matchbox.mappinglanguage.MatchboxStructureMapUtilities) structureMapUtilities).getEngine().getValidator(
				Manager.FhirFormat.JSON);
		 } catch (IOException e) {
			throw new NotImplementedException("Not done yet (FFHIRPathHostServices.conformsToProfile), engine could not be created");
		 }
		List<ValidationMessage> valerrors = new ArrayList<ValidationMessage>();
		if (item instanceof Resource) {
			val.validate(appContext, valerrors, (Resource) item, url);
			return noErrorValidationMessages(valerrors);
		}
		if (item instanceof Element) {
			val.validate(appContext, valerrors, null, (Element) item, url);
			return noErrorValidationMessages(valerrors);
		}
		throw new NotImplementedException("Not done yet (FFHIRPathHostServices.conformsToProfile), when item is not element or not resource");
	}


	@Override
	public ValueSet resolveValueSet(FHIRPathEngine engine, Object appContext, String url) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'resolveValueSet'");
	}

	@Override
	public boolean paramIsType(String name, int index) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'paramIsType'");
	}

}
