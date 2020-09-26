package ca.uhn.fhir.jpa.empi;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class EmpiConfigCondition implements Condition {
  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
    String property = conditionContext.getEnvironment().getProperty("hapi.fhir.empi_enabled");
    return Boolean.parseBoolean(property);
  }
}
