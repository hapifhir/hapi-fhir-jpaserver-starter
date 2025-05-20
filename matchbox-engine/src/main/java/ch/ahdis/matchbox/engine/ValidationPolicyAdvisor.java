package ch.ahdis.matchbox.engine;


import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.r5.utils.validation.IValidationPolicyAdvisor;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.validation.instance.advisor.BasePolicyAdvisorForFullValidation;
import org.hl7.fhir.validation.instance.InstanceValidator;

public class ValidationPolicyAdvisor extends BasePolicyAdvisorForFullValidation {

	protected static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ValidationPolicyAdvisor.class);
    private final Set<PathAndMessageId> messagesToIgnore = new HashSet<>();

		
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
        // Check if this specific path+messageId combination should be ignored
        if (messagesToIgnore.contains(new PathAndMessageId(path, messageId))) {
            return true;
        }
        return false;
    }
    
    public void addSuppressedError(String path, String messageId) {
        if (path != null && messageId != null) {
            messagesToIgnore.add(new PathAndMessageId(path, messageId));
            log.debug("Added message to ignore - path: {} messageId: {}", path, messageId);
        }
    }
    
    public void clearErrorMessagesToIgnore() {
      messagesToIgnore.clear();
    }

    /**
     * Helper class for storing path and messageId combinations with proper equals/hashCode
     */
    private static class PathAndMessageId {
        private final String path;
        private final String messageId;
        
        public PathAndMessageId(String path, String messageId) {
            this.path = path;
            this.messageId = messageId;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PathAndMessageId that = (PathAndMessageId) o;
            return Objects.equals(path, that.path) && Objects.equals(messageId, that.messageId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(path, messageId);
        }
    }


}
