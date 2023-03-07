package com.iprd.fhir.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.jpa.starter.service.CachingService;

public class OperationHelper {
	private static final Logger logger = LoggerFactory.getLogger(OperationHelper.class);

    public static void doWithRetry(int maxAttempts, Operation operation) {
        for (int count = 0; count < maxAttempts; count++) {
            try {
                operation.doIt();
                count = maxAttempts; //don't retry
            } catch (Exception e) {
            	operation.handleException(e);
            	logger.warn(String.valueOf(count)+" attempt sleeping before reattempt");
            }
        }
    }
}