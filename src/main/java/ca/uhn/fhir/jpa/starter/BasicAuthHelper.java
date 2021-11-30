package ca.uhn.fhir.jpa.starter;

import java.util.Base64;

public class BasicAuthHelper {

  public boolean isValid(String basicAuthUsername, String basicAuthPass, String basicAuthToken) {
    String unEncodedUsernamePass = basicAuthUsername + ":" + basicAuthPass;
    String encodedUsernamePass = Base64.getEncoder().encodeToString(unEncodedUsernamePass.getBytes());
    return encodedUsernamePass.equals(basicAuthToken);
  }
}
