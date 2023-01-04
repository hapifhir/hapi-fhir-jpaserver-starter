package ca.uhn.fhir.jpa.starter.interceptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.hl7.fhir.r4.model.IdType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import ca.uhn.fhir.i18n.Msg;
import ca.uhn.fhir.jpa.starter.AppProperties;
import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;


@SuppressWarnings("ConstantConditions")
public class DC4HAuthorizationInterceptor extends AuthorizationInterceptor {
   private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(DC4HAuthorizationInterceptor.class);
   private AppProperties appProperties = null;

   public DC4HAuthorizationInterceptor(AppProperties appProperties) {
      this.appProperties = appProperties;
   }


   public String[] parseAuthorities(String jsonLine) {
      JsonElement jelement = new JsonParser().parse(jsonLine);
      JsonObject  jobject = jelement.getAsJsonObject();
      //jobject = jobject.getAsJsonObject("authorities");
      JsonArray jarray = jobject.getAsJsonArray(this.appProperties.getAuthorization_token_claim_name());
      String[] authorities = null;
      if (jarray!=null) {
         authorities = new String[jarray.size()];
         for(int i = 0; i < jarray.size(); i++){
            authorities[i] = jarray.get(i).getAsString();
         }
      }
      return authorities;
      
      //String result = jobject.get("translatedText").getAsString();
      //return result;
  }

   @Override
   public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {

      // Process authorization header - The following is a fake
      // implementation. Obviously we'd want something more real
      // for a production scenario.
      //
      // In this basic example we have two hardcoded bearer tokens,
      // one which is for a user that has access to one patient, and
      // another that has full access.
      String authHeader = theRequestDetails.getHeader("Authorization");
      if (authHeader!=null && authHeader.startsWith("Bearer")) {
         ourLog.info("Got a Bearer token, so processing");

         String jwtToken = authHeader.replace("Bearer ", "");

         ourLog.info("Clean bearer token is " + jwtToken);

         String[] jwtParts = jwtToken.split("\\.");

         if (jwtParts.length < 2) {
            ourLog.error("JWT token didn't split into 3 parts");
            throw new AuthenticationException(Msg.code(644) + "Invalid Authorization header value");
         }
         String jwtPayloadBase64 = jwtParts[1];
         
         byte[] decodedBytes = Base64.getDecoder().decode(jwtPayloadBase64);
         String decodedPayload = new String(decodedBytes);
         
         String[] authorities = null;
         authorities = parseAuthorities(decodedPayload);

         if (authorities!=null) {
            if (theRequestDetails.getRequestType().equals(RequestTypeEnum.GET) || theRequestDetails.getRequestType().equals(RequestTypeEnum.OPTIONS) || theRequestDetails.getRequestType().equals(RequestTypeEnum.HEAD)) {
               ourLog.info(theRequestDetails.getRequestType().toString() + " so checking for " + this.appProperties.getAuthorization_token_read_perm() + " in claim");
               boolean contains = Arrays.stream(authorities).anyMatch(this.appProperties.getAuthorization_token_read_perm()::equals);
               if (contains) {
                  return new RuleBuilder()
                     .allow().read().allResources().withAnyId()
                     .build();
               } else {
                  throw new AuthenticationException(Msg.code(644) + "Insufficient permission for this operation");
               }
            } else {
               ourLog.info(theRequestDetails.getRequestType().toString() + " so checking for " + this.appProperties.getAuthorization_token_write_perm() + " in claim");
               boolean contains = Arrays.stream(authorities).anyMatch(this.appProperties.getAuthorization_token_write_perm()::equals);
               if (contains) {
                  return new RuleBuilder()
                     .allow().write().allResources().withAnyId()
                     .build();
               } else {
                  throw new AuthenticationException(Msg.code(644) + "Insufficient permission for this operation");
               }
            }
         }
      } else {
         ourLog.info(this.appProperties.getAuthorization_token_claim_name() + " not present in token");
      }

      throw new AuthenticationException(Msg.code(644) + "Missing or invalid Authorization header value");
      
/* 
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
*/

   }


}