package ca.uhn.fhir.jpa.starter.validation;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration that enables versioned URL fallback behavior for FHIR validation.
 *
 * This wraps the validation support chain to add fallback logic for versioned canonical URLs.
 * When a versioned URL like "http://hl7.org/fhir/StructureDefinition/Organization|4.0.1"
 * cannot be found, it will automatically fall back to:
 * 1. Major.minor version (e.g., |4.0)
 * 2. Non-versioned URL (without the |version suffix)
 *
 * This is useful when Implementation Guides reference versioned base FHIR resources
 * that aren't loaded with exact version matching.
 */
@Configuration
public class VersionedUrlFallbackConfig {

    private static final Logger ourLog = LoggerFactory.getLogger(VersionedUrlFallbackConfig.class);

    public VersionedUrlFallbackConfig(FhirContext theFhirContext, ValidationSupportChain theValidationSupportChain) {
        ourLog.info("Adding VersionedUrlFallbackValidationSupport to validation chain");
        theValidationSupportChain.addValidationSupport(0,
                new VersionedUrlFallbackValidationSupport(theFhirContext, theValidationSupportChain));
    }
}
