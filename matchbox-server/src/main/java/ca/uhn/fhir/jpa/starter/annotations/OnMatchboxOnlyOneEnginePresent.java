package ca.uhn.fhir.jpa.starter.annotations;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.env.Environment;


public class OnMatchboxOnlyOneEnginePresent implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {

		Environment env = conditionContext.getEnvironment();
  
 	   String propertyValue = env.getProperty("matchbox.fhir.context.onlyOneEngine");
        
        // Return true if the property exists and is set to true
    	return propertyValue != null && Boolean.parseBoolean(propertyValue);
  	}
}