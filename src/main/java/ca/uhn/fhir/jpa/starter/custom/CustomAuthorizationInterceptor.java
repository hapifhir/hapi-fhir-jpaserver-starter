package ca.uhn.fhir.jpa.starter.custom;

import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;

import java.io.ByteArrayInputStream;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;

import ca.uhn.fhir.interceptor.api.Pointcut;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

public class CustomAuthorizationInterceptor extends AuthorizationInterceptor {

    public CustomAuthorizationInterceptor() {
        System.out.println("\n==> CustomAuthorizationInterceptor | Configured in StarterJpaConfig.java" + "\n");
    }

    @Override
    public List<IAuthRule> buildRuleList(RequestDetails request) {
        logIncomingRequest(request);

        try {
            String stackType = stackCheckType(request);

            if (stackType == CustomEnvironment.stackType_ICMR)
                icmrStackAuth(request);

            /*
             * Add Authentication & Authorization methods here
             */
        } catch (Exception e) {
            System.err.println("\n==> | ERROR | " + e.getMessage() + "\n");
            return new RuleBuilder().denyAll(e.getMessage()).build();
        }

        return new RuleBuilder().allowAll().build();
    }

    public static String stackCheckType(RequestDetails request) {
        ArrayList<String> basePathItems_al = new ArrayList<>(Arrays.asList(request.getRequestPath().split("/")));
        for (String basePathItem : basePathItems_al) {
            for (String resTypeItem : CustomEnvironment.resourceTypes_al) {
                if (basePathItem.equalsIgnoreCase(resTypeItem)) {
                    return CustomEnvironment.stackType_ICMR;
                }
            }
        }
        return CustomEnvironment.stackType_FHIR;
    }

    public static void icmrStackAuth(RequestDetails request) throws Exception {
        Map<String, Object> queryParams = extractQueryParams(request);
        checkMandatoryHeaders(request);
        checkJWTCertificate();
        DecodedJWT decodedJWT = verifyJWTToken(request.getHeader("Authorization"));
        verifyRid(decodedJWT, queryParams.get("rid").toString());
    }

    public static void checkMandatoryHeaders(RequestDetails request) throws Exception {
        String JwtToken = request.getHeader("Authorization");

        if (JwtToken == null || JwtToken.length() == 0) {
            throw new Exception("Missing authorization token.");
        }
    }

    public static Map<String, Object> extractQueryParams(RequestDetails request) throws Exception {
        Map<String, Object> queryParams = new HashMap<String, Object>();

        queryParams.put("rid", request.getRequestPath().split("/")[1]);

        if (queryParams.get("rid") == null || queryParams.get("rid").toString().length() == 0) {
            throw new Exception("Missing ID or Error processing ID.");
        }

        return queryParams;
    }

    public static void checkJWTCertificate() throws Exception {
        if (CustomEnvironment.JWT_CERT == null) {
            CustomStartupService custStartupService = new CustomStartupService();
            custStartupService.fetchJWTCertificate();

            if (CustomEnvironment.JWT_CERT == null || CustomEnvironment.JWT_CERT.length() == 0)
                throw new Exception("Missing authorization certificate.");
        }
    }

    public static DecodedJWT verifyJWTToken(String JwtToken) throws Exception {
        String JwtCert = CustomEnvironment.JWT_CERT;

        // Eliminate "Bearer " prefix
        if (JwtToken.startsWith("Bearer ")) {
            JwtToken = JwtToken.split("Bearer ")[1];
        }

        // Extract Public Key from JWT certificate
        X509Certificate certificate = loadCertificate(JwtCert);
        PublicKey publicKey = certificate.getPublicKey();

        if (publicKey instanceof ECPublicKey) {
            ECPublicKey ecPublicKey = (ECPublicKey) publicKey;

            // Define the ES256 algorithm with the ECDSA public key
            Algorithm algorithm = Algorithm.ECDSA256(ecPublicKey, null);

            // Create JWT verifier
            JWTVerifier verifier = JWT.require(algorithm).build();

            // Verify hash of the JWT Token using the Auth Certificate
            DecodedJWT decodedJWT = verifier.verify(JwtToken);

            // System.out.println("\n==> Token is valid");
            // System.out.println("==> Subject: " + decodedJWT.getSubject());
            // System.out.println("==> Issuer: " + decodedJWT.getIssuer());
            // System.out.println("==> Expires at: " + decodedJWT.getExpiresAt());
            // System.out.println("==> IID: " + decodedJWT.getClaim("iid").asString());

            return decodedJWT;
        } else {
            throw new Exception("The certificate does not contain an ECDSA public key.");
        }
    }

    // Verify if ResourceID is same in queryparam and JWT Token
    public static void verifyRid(DecodedJWT decodedJWT, String Rid) throws Exception {
        String Iid = decodedJWT.getClaim("iid").asString().split("ri:")[1];

        if (!Rid.equals(Iid))
            throw new Exception("ID in query does not match ID in the token.");
    }

    public static X509Certificate loadCertificate(String certificateString) throws Exception {
        // Decode the base64 encoded certificate string
        byte[] decoded = Base64.getDecoder().decode(certificateString.replace("-----BEGIN CERTIFICATE-----", "")
                .replace("-----END CERTIFICATE-----", "").replace("\n", ""));
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decoded);

        // Load certificate from byte array
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(byteArrayInputStream);
    }

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void logIncomingRequest(RequestDetails request) {
        System.out.println("\n");
        System.out.println("=====>  Custom Request Authorization Interceptor  <=====");
        System.out.println("- Type : " + request.getRequestType());
        System.out.println("- URL : " + request.getCompleteUrl());
        System.out.println("- Base URL : " + request.getFhirServerBase());
        System.out.println("- Path : " + request.getRequestPath());
        System.out.println("- ID : " + request.getRequestId());
        System.out.println("\n");
    }
}
