package ch.ahdis.matchbox.interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.server.method.BaseMethodBinding;

@Interceptor
public class MatchboxValidationInterceptor {

	private static final Logger ourLog = LoggerFactory.getLogger(MatchboxValidationInterceptor.class);

	public FhirContext theContext;
	
	/**
	 * Constructor
	 */
	public MatchboxValidationInterceptor(FhirContext theContext) {
		this.theContext = theContext;
		ourLog.debug("Interceptor for adjusting capability statement");
	}

	@Hook(Pointcut.SERVER_PROVIDER_METHOD_BOUND)
	public BaseMethodBinding bindMethod(BaseMethodBinding theMethodBinding) {

		String resourceName = theMethodBinding.getResourceName();
		RestOperationTypeEnum restOperationType = theMethodBinding.getRestOperationType();
		String methodName = theMethodBinding.getMethod().getName();
		
		switch (restOperationType) {
			case EXTENDED_OPERATION_SERVER:
				if ("validate".equals(methodName) || "installNpmPackage".equals(methodName)) {
					return theMethodBinding;
				}
				return null;
			case EXTENDED_OPERATION_TYPE:
			case EXTENDED_OPERATION_INSTANCE: {
				if ("validate".equals(methodName)) {
					return null;
				}
				break;
			}
			case READ:
			case VREAD:
				// if (resourceName.equals("QuestionnaireResponse")) {
				// 	return null;
				// }
				return theMethodBinding;
			case UPDATE:
			case CREATE:
				// if (resourceName.equals("StructureMap") || resourceName.equals("ImplementationGuide") || resourceName.equals("Questionnaire") ) {
					return theMethodBinding;
				// }
				// return null;
			case DELETE:
				if (resourceName.equals("ImplementationGuide") ) {
					return theMethodBinding;
				}
				return null;
			case SEARCH_TYPE:
				return theMethodBinding;
			default:
				return null;
		}
		switch(methodName) {
		  case "metaDelete":
		  case "metaAdd":
		  case "meta":
		  case "expunge":
		  	return null;
		}
		return theMethodBinding;
	}

}
