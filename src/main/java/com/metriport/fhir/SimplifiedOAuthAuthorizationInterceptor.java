package com.metriport.fhir;

import java.util.List;

import org.hl7.fhir.r4.model.IdType;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationConstants;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

/**
 * A simplified AuthorizationInterceptor that relies on an upstream JWT validator.
 * This class does not validade the JWT is signed accordingly, it assumes this is
 * verified by an upstream service like API Gateway + Lambdas.
 * It only decodes the JWT and verifies the user has access to the requested resource
 * and operation.
 */
@SuppressWarnings("ConstantConditions")
@Interceptor(order = AuthorizationConstants.ORDER_AUTH_INTERCEPTOR)
public class SimplifiedOAuthAuthorizationInterceptor extends AuthorizationInterceptor {

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory
            .getLogger(SimplifiedOAuthAuthorizationInterceptor.class);

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

        // TODO refactor this
        ourLog.info("[AUTH] Validating request {}", theRequestDetails.getParameters());

        /*
         * Assuming this server is behind API Gateway or similar upstream service, which
         * is also responsible for verifying the JWT:
         * - get the JWT from the request
         * - parse/decode it
         * - verify if the claims either
         * - match what's being requested
         * - is an admin claim
         * - fail otherwise
         */

        // From
        // https://hapifhir.io/hapi-fhir/docs/security/authorization_interceptor.html
        // Process authorization header - The following is a fake
        // implementation. Obviously we'd want something more real
        // for a production scenario.
        //
        // In this basic example we have two hardcoded bearer tokens,
        // one which is for a user that has access to one patient, and
        // another that has full access.
        IdType userIdPatientId = null;
        boolean userIsAdmin = false;
        String authHeader = theRequestDetails.getHeader("Authorization");
        if ("Bearer dfw98h38r".equals(authHeader)) {
            // This user has access only to Patient/1 resources
            userIdPatientId = new IdType("Patient", 1L);
        } else if ("Bearer 39ff939jgg".equals(authHeader)) {
            // This user has access to everything
            userIsAdmin = true;
        } else {
            // Throw an HTTP 401
            throw new AuthenticationException(Msg.code(644) + "Missing or invalid Authorization header value");
        }

        // If the user is a specific patient, we create the following rule chain:
        // Allow the user to read anything in their own patient compartment
        // Allow the user to write anything in their own patient compartment
        // If a client request doesn't pass either of the above, deny it
        if (userIdPatientId != null) {
            return new RuleBuilder()
                    .allow().read().allResources().inCompartment("Patient", userIdPatientId).andThen()
                    .allow().write().allResources().inCompartment("Patient", userIdPatientId).andThen()
                    .denyAll()
                    .build();
        }

        // If the user is an admin, allow everything
        if (userIsAdmin) {
            return new RuleBuilder()
                    .allowAll()
                    .build();
        }

        // By default, deny everything. This should never get hit, but it's
        // good to be defensive
        return new RuleBuilder()
                .denyAll()
                .build();
    }
}