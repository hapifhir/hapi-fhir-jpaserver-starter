package ca.uhn.fhir.jpa.starter.tester.interceptor;

import ca.uhn.fhir.rest.api.Constants;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;

public final class AuthorizationHeaderAuthInterceptor  implements IClientInterceptor {

    private final String header;

    public AuthorizationHeaderAuthInterceptor(String header) {
        this.header = header;
    }

    @Override
    public void interceptRequest(IHttpRequest theRequest) {
        theRequest.addHeader(Constants.HEADER_AUTHORIZATION, header);
    }

    @Override
    public void interceptResponse(IHttpResponse theResponse) {
        // nothing
    }
}
