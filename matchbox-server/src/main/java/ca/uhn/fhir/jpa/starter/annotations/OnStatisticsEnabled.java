package ca.uhn.fhir.jpa.starter.annotations;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnStatisticsEnabled implements Condition {
  
  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {

    Environment env = conditionContext.getEnvironment();

    String propertyValue = env.getProperty("matchbox.validation.save-statistics");

    // Return true if the property exists and is set to true
    return propertyValue != null && Boolean.parseBoolean(propertyValue);
  }
}
