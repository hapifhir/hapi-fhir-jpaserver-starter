package ca.uhn.fhir.jpa.starter.cql;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CqlConfigCondition implements Condition {

  @Override
  public boolean matches(ConditionContext theConditionContext, AnnotatedTypeMetadata theAnnotatedTypeMetadata) {
    String property = theConditionContext.getEnvironment().getProperty("hapi.fhir.cql_enabled");
	  return Boolean.parseBoolean(property);
  }
}
