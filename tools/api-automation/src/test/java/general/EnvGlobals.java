package general;


import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class EnvGlobals {
    public static StringBuilder difference = new StringBuilder();
    public static RequestSpecification requestSpecification;
    public static Response response;

    public EnvGlobals() {
    }
}