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
            return ReferenceValidationPolicy.CHECK_EXISTS_AND_TYPE;
        }                                                        
        return ReferenceValidationPolicy.IGNORE;
    }

}
