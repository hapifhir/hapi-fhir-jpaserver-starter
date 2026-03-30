package ch.ahdis.matchbox.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.server.method.BaseMethodBinding;

@Interceptor
public class MatchboxValidationInterceptor {

	/**
	 * This method is called to allow or block the binding of a method to a particular REST operation.
	 * By returning null, the method will not be bound to that operation.
	 * By returning the original method binding, the method will be bound as normal.
	 */
	@Hook(Pointcut.SERVER_PROVIDER_METHOD_BOUND)
	public BaseMethodBinding bindMethod(BaseMethodBinding theMethodBinding) {
		final String resourceName = theMethodBinding.getResourceName();
		final RestOperationTypeEnum restOperationType = theMethodBinding.getRestOperationType();
		final String methodName = theMethodBinding.getMethod().getName();

		switch (restOperationType) {
			case EXTENDED_OPERATION_SERVER:
				if ("validate".equals(methodName) || "installNpmPackage".equals(methodName)) {
					return theMethodBinding;
				}
				return null;
			case EXTENDED_OPERATION_TYPE, EXTENDED_OPERATION_INSTANCE: {
				if ("validate".equals(methodName)) {
					return null;
				}
				break;
			}
			case READ, VREAD:
				// if (resourceName.equals("QuestionnaireResponse")) {
				// 	return null;
				// }
				return theMethodBinding;
			case UPDATE, CREATE:
				// if (resourceName.equals("StructureMap") || resourceName.equals("ImplementationGuide") || resourceName.equals("Questionnaire") ) {
					return theMethodBinding;
				// }
				// return null;
			case DELETE:
				if (resourceName.equals("ImplementationGuide") ) {
					return theMethodBinding;
				}
				return null;
			case SEARCH_TYPE, GET_PAGE:
				return theMethodBinding;
			default:
				return null;
		}
		return switch (methodName) {
			case "metaDelete", "metaAdd", "meta", "expunge" -> null;
			default -> theMethodBinding;
		};
	}

}
