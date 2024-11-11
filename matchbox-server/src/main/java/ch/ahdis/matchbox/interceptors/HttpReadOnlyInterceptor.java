package ch.ahdis.matchbox.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.ForbiddenOperationException;

/**
 * An interceptor that denies authorization for any request that would modify data on the server.
 *
 * @author Quentin Ligier
 **/
@Interceptor
public class HttpReadOnlyInterceptor {

	@Hook(Pointcut.SERVER_INCOMING_REQUEST_POST_PROCESSED)
	public boolean incomingRequestPostProcessed(final RequestDetails theRequestDetails)
		throws ForbiddenOperationException {

		switch (theRequestDetails.getRestOperationType()) {
			case CREATE, DELETE, UPDATE, PATCH, UPDATE_REWRITE_HISTORY,
				TRANSACTION, BATCH,
				ADD_TAGS, DELETE_TAGS,
				META_ADD, META_DELETE:
				throw new ForbiddenOperationException("This server is read-only");
			default:
				// Everything else is authorized
		}
		// $validate is EXTENDED_OPERATION_TYPE, with theRequestDetails.getOperation() = '$validate'
		// Same for $transform

		return true;
	}
}
