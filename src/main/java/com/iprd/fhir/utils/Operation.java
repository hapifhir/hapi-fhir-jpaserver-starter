package com.iprd.fhir.utils;

public abstract class Operation {
    abstract public void doIt();
    public void handleException(Exception cause) {
        //default impl: do nothing, log the exception, etc.
    	cause.printStackTrace();
    }
}
