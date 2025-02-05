package ch.ahdis.matchbox.engine;


import org.hl7.fhir.r5.utils.validation.IResourceValidator;
import org.hl7.fhir.r5.utils.validation.constants.ReferenceValidationPolicy;
import org.hl7.fhir.validation.instance.advisor.BasePolicyAdvisorForFullValidation;
import org.hl7.fhir.validation.instance.InstanceValidator;

public class ValidationPolicyAdvisor extends BasePolicyAdvisorForFullValidation {

    public ValidationPolicyAdvisor(ReferenceValidationPolicy refpol) {
        super(refpol);
    }

    @Override
    public ReferenceValidationPolicy policyForReference(IResourceValidator validator, Object appContext, String path, String url) {
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
      return false;
    }

}
