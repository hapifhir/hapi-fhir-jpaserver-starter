package ca.uhn.fhir.jpa.starter.cql;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CqlConfigCondition implements Condition {

  @Override
  public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
    String property = conditionContext.getEnvironment().getProperty("hapi.fhir.cql_enabled");
    boolean enabled = Boolean.parseBoolean(property);
    return enabled;
  }
}
