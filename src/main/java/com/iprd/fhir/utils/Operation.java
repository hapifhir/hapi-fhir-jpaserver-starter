package com.iprd.fhir.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Operation {

	private static final Logger logger = LoggerFactory.getLogger(Operation.class);
    abstract public void doIt();
    public void handleException(Exception ex) {
        //default impl: do nothing, log the exception, etc.
		 logger.warn(ExceptionUtils.getStackTrace(ex));
    }
}
