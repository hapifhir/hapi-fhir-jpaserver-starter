package ca.uhn.fhir.jpa.starter.auth.service;


import ca.uhn.fhir.jpa.starter.auth.models.AuthApiResponse;
import graphql.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class AuthServiceImpl implements AuthService {

    @Value("${hapi.fhir.ngsa.auth.server.url}")
    private String authServerUrl;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    public Pair<Boolean, String> isTokenValid(String token) {

        if (authServerUrl == null) {
            logger.error("NGSA auth url is null");
            return new Pair<>(false, null);
        }

        AuthRestTemplate authRestTemplate = new AuthRestTemplate(new RestTemplate());
        try {
            String url = authServerUrl + "/api/v1/auth/verify-token";

            AuthApiResponse authApiResponse = authRestTemplate.verifyToken(url + "?token=" + token);
            if (authApiResponse != null) {
                logger.info("Success verifying ngsa token");
                return new Pair<>(authApiResponse.getSuccess(), authApiResponse.getData().getUsername());
            } else {
                logger.error("Error verifying ngsa token; token is null");
                return new Pair<>(false, null);
            }
        } catch (RestClientException e) {
            logger.error("Error verifying ngsa token:" + e.getLocalizedMessage(), e);
            return new Pair<>(false, null);
        }


    }
}