package ch.ahdis.matchbox.engine;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.r5.utils.validation.IValidationPolicyAdvisor;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.validation.instance.advisor.BasePolicyAdvisorForFullValidation;
import org.hl7.fhir.validation.instance.InstanceValidator;

public class ValidationPolicyAdvisor extends BasePolicyAdvisorForFullValidation {

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationPolicyAdvisor.class);
    
    // This is a map of messageId to a list of regex paths that should be ignored
    private final Map<String, List<String>> messagesToIgnore = new HashMap<>();
		
    public ValidationPolicyAdvisor(ReferenceValidationPolicy refpol) {
        super(refpol);
    }

    @Override
    public ReferenceValidationPolicy policyForReference(IResourceValidator validator, Object appContext, String path,
																		  String url, IValidationPolicyAdvisor.ReferenceDestinationType destinationType) {
        if (url!=null && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("urn:"))) {
            if (validator instanceof InstanceValidator && ((InstanceValidator)validator).isAllowExamples() && (url.contains("example.org") || url.contains("acme.com") || url.contains("acme.org"))) {
                return ReferenceValidationPolicy.IGNORE;               
            }
            if (path!=null && path.startsWith("Bundle")) {
               // we wan't to skip references in Bundles
               // see also https://github.com/ahdis/matchbox/issues/345
               // Bundle.entry[0].resource/*Composition/33A38B65386A62CCE8446299FC166D60F8620EC6*/.subject
					// Document reference requirements are checked in
            	return ReferenceValidationPolicy.IGNORE;
            }
            return ReferenceValidationPolicy.CHECK_VALID;
        }
        return ReferenceValidationPolicy.IGNORE;
    }
    
    @Override
    public boolean isSuppressMessageId(String path, String messageId) {
        log.debug("Checking suppression for path: {} messageId: {}", path, messageId);
        List<String> pathsToCheck = messagesToIgnore.get(messageId);
        if (pathsToCheck != null && !pathsToCheck.isEmpty()) {
            for (String pathToCheck : pathsToCheck) {
                if (path.matches(pathToCheck)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Add a messageId to the list of messages that should be ignored for a given path.
     * 
     * @param messageId The messageId to ignore, see 
     * @param regexPath The regex to check the path against
     */
    public void addSuppressedError(String messageId, String regexPath) {
        List<String> messagesToCheck = messagesToIgnore.get(messageId);
        if (messagesToCheck == null) {
            List<String> newList = new ArrayList<>();
            newList.add(regexPath);
            messagesToIgnore.put(messageId, newList);
        } else {
            messagesToCheck.add(regexPath);
        }
    }

    /** returns all error regex pattern to ignore for the messagesId */
    public List<String> getSuppressedErrorMessages() {
        return messagesToIgnore.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream()
            .map(regex -> entry.getKey() + ":" + regex))
            .collect(java.util.stream.Collectors.toList());
    }
    
    public void clearErrorMessagesToIgnore() {
      messagesToIgnore.clear();
    }

}
