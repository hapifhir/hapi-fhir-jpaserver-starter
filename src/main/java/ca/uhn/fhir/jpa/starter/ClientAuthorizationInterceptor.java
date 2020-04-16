package  ca.uhn.fhir.jpa.starter;


import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.interceptor.auth.AuthorizationInterceptor;
import ca.uhn.fhir.rest.server.interceptor.auth.IAuthRule;
import ca.uhn.fhir.rest.server.interceptor.auth.RuleBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import java.net.HttpURLConnection;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.*;
import java.util.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

@SuppressWarnings("ConstantConditions")
public class ClientAuthorizationInterceptor extends AuthorizationInterceptor {
  static final org.slf4j.Logger ourLog = LoggerFactory.getLogger(ClientAuthorizationInterceptor.class);
  String introspectUrl = "https://auth.mettles.com:8443/auth/realms/ProviderCredentials/protocol/openid-connect/token/introspect";

  @Override
  public List<IAuthRule> buildRuleList(RequestDetails theRequestDetails) {
    String useOauth = "false";
    System.out.println("\n\n\n\n\n\n\n Boolean.parseBoolean(useOauth)");
    System.out.println(Boolean.parseBoolean(useOauth));
//    ourLog.info("\n\n\n 111111111 boolean Parseeeses","testt");
//    ourLog.info("Request sessioooos parametewrs");
//    System.out.println("is metadata ?? \n");
//    System.out.println(theRequestDetails.getCompleteUrl());
//    System.out.println("http://54.227.173.76:8181/fhir/baseDstu3/metadata");
//    System.out.println(theRequestDetails.getCompleteUrl().trim().endsWith("baseDstu3/metadata"));
//    System.out.println(theRequestDetails.getRequestType().toString()== "GET");
    if(theRequestDetails.getCompleteUrl().trim().endsWith("baseDstu3/metadata") && theRequestDetails.getRequestType().toString()== "GET" ) {
    	return new RuleBuilder()
    	          .allowAll()
    	          .build();
    }
    if (!Boolean.parseBoolean(useOauth)) {
      return new RuleBuilder()
          .allowAll()
          .build();
    }
    CloseableHttpClient client = HttpClients.createDefault();
    String authHeader = theRequestDetails.getHeader("Authorization");
    // Get the token and drop the "Bearer"
    if (authHeader == null) {
      return new RuleBuilder()
          .denyAll("No authorization header present")
          .build();
    }
    String token = authHeader.split(" ")[1];
    String secret = "48bf2c3e-2bd6-4f8d-a5ce-2f94adcb7492";
    String clientId = "app-token";
    HttpPost httpPost = new HttpPost(introspectUrl);
    List<NameValuePair> params = new ArrayList<NameValuePair>();
    params.add(new BasicNameValuePair("client_id", clientId));
    params.add(new BasicNameValuePair("client_secret", secret));
    params.add(new BasicNameValuePair("token", token));
    try {
      httpPost.setEntity(new UrlEncodedFormEntity(params));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
//    System.out.println("introspectUrl::");
//    System.out.println(introspectUrl);
    JsonObject jsonResponse;
    // Map<String,Object> params = new LinkedHashMap<>();
    try {
      CloseableHttpResponse response = client.execute(httpPost);
      String jsonString = EntityUtils.toString(response.getEntity());
      jsonResponse = new JsonParser().parse(jsonString).getAsJsonObject();
      client.close();
     // 	URL url = new URL(introspectUrl);
     //  params.put("client_id",clientId);
     //  params.put("client_secret", secret);
     //  params.put("token", token);
     //  StringBuilder postData = new StringBuilder();
     //  for (Map.Entry<String,Object> param : params.entrySet()) {
     //      if (postData.length() != 0) postData.append('&');
     //      postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
     //      postData.append('=');
     //      postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
     //  }
     //  byte[] postDataBytes = postData.toString().getBytes("UTF-8");

     //  HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();

     //  conn.setRequestMethod("POST");
     //  conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
     //  conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
     //  conn.setDoOutput(true);
     //  conn.getOutputStream().write(postDataBytes);

     //  Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
     //  System.out.print("\n\n\n\n\n\n\n\n\n\n\n\noutput");
     //  for (int c; (c = in.read()) >= 0;){
     //      System.out.print((char)c);
    	// }

    } catch (IOException e) {
//      System.out.println("\n\n\\n\n\n\\n\n\n\n\nEXceptionnnnnn");
      e.printStackTrace();
      jsonResponse = null;
    }
//    System.out.println("\n\n\n\n params");
//    System.out.println(params);
//    System.out.println(jsonResponse);
    ourLog.info("Response:");
//    ourLog.info(params.toString());
    ourLog.info(jsonResponse.toString());

     if (jsonResponse.get("active").getAsBoolean()) {
       return new RuleBuilder()
           .allowAll()
           .build();
     } else {
       return new RuleBuilder()
           .denyAll("Rejected OAuth token - failed to introspect")
           .build();
     }
//    return new RuleBuilder()
//          .allowAll()
//          .build();
  }


}
