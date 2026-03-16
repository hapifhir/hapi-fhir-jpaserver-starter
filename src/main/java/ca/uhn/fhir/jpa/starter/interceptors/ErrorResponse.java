package ca.uhn.fhir.jpa.starter.interceptors;

public class ErrorResponse {
    public int httpCode;
    public int errorCode;
    public String errorName;
    public String message;
    public Object data;
    public String[] stack;
    public String details;
}
