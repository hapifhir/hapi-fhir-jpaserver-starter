package ch.ahdis.fhir.hapi.jpa.validation;

import ca.uhn.fhir.context.BaseRuntimeChildDefinition;
import ca.uhn.fhir.context.BaseRuntimeElementCompositeDefinition;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.util.OperationOutcomeUtil;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import org.hl7.fhir.instance.model.api.IBase;
import org.hl7.fhir.instance.model.api.IBaseExtension;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IPrimitiveType;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class ValidationResultWithExtensions extends ValidationResult {

	private final List<SingleValidationMessage> myMessages;

	private final FhirContext myCtx;

//  public static void addLocationToIssue(FhirContext theContext, IBase theIssue, String theLocation) {
//    if (isNotBlank(theLocation)) {
//      BaseRuntimeElementCompositeDefinition<?> issueElement = (BaseRuntimeElementCompositeDefinition<?>) theContext.getElementDefinition(theIssue.getClass());
//      BaseRuntimeChildDefinition locationChild = issueElement.getChildByName("location");
//      IPrimitiveType<?> locationElem = (IPrimitiveType<?>) locationChild.getChildByName("location").newInstance(locationChild.getInstanceConstructorArguments());
//      locationElem.setValueAsString(theLocation);
//      locationChild.getMutator().addValue(theIssue, locationElem);
//    }
//  }


	public ValidationResultWithExtensions(FhirContext theCtx, List<SingleValidationMessage> theMessages) {
		super(theCtx, theMessages);
		myCtx = theCtx;
		myMessages = theMessages;
	}

	public void addLocationLineToIssue(FhirContext theContext, IBase theIssue, int theLocation) {
		BaseRuntimeElementCompositeDefinition<?> issueElement = (BaseRuntimeElementCompositeDefinition<?>) theContext.getElementDefinition(
			theIssue.getClass());
		BaseRuntimeChildDefinition extensionChild = issueElement.getChildByName("extension");

		IBaseExtension<?, ?> extensionElem = (IBaseExtension<?, ?>) extensionChild.getChildByName("extension").newInstance(
			extensionChild.getInstanceConstructorArguments());
		extensionElem.setUrl("http://hl7.org/fhir/StructureDefinition/operationoutcome-issue-line");

		IPrimitiveType<Integer> value = (IPrimitiveType<Integer>) theContext.getElementDefinition("integer").newInstance();
		value.setValue(theLocation);
		extensionElem.setValue(value);

		extensionChild.getMutator().addValue(theIssue, extensionElem);
	}

	@Override
	public void populateOperationOutcome(IBaseOperationOutcome theOperationOutcome) {
		for (SingleValidationMessage next : myMessages) {
			String location;
			if (isNotBlank(next.getLocationString())) {
				location = next.getLocationString();
			} else if (next.getLocationLine() != null || next.getLocationCol() != null) {
				location = "Line[" + next.getLocationLine() + "] Col[" + next.getLocationCol() + "]";
			} else {
				location = null;
			}
			String severity = next.getSeverity() != null ? next.getSeverity().getCode() : null;
			IBase issue = OperationOutcomeUtil.addIssue(myCtx, theOperationOutcome, severity, next.getMessage(), location,
																	  Constants.OO_INFOSTATUS_PROCESSING);

			if (next.getLocationLine() != null || next.getLocationCol() != null) {
				String unknown = "(unknown)";
				String line = unknown;
				if (next.getLocationLine() != null && next.getLocationLine() != -1) {
					this.addLocationLineToIssue(myCtx, issue, next.getLocationLine());
					line = next.getLocationLine().toString();
				}
				String col = unknown;
				if (next.getLocationCol() != null && next.getLocationCol() != -1) {
					col = next.getLocationCol().toString();
				}
				if (!unknown.equals(line) || !unknown.equals(col)) {
					OperationOutcomeUtil.addLocationToIssue(myCtx, issue, "Line " + line + ", Col " + col);
				}
			}
		}

		if (myMessages.isEmpty()) {
			String message = myCtx.getLocalizer().getMessage(ValidationResult.class, "noIssuesDetected");
			OperationOutcomeUtil.addIssue(myCtx, theOperationOutcome, "information", message, null, "informational");
		}
	}


}
