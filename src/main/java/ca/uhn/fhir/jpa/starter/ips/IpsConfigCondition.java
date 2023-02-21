package ca.uhn.fhir.jpa.starter.ips;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class IpsConfigCondition implements Condition {

	@Override
  public boolean matches(ConditionContext theConditionContext, AnnotatedTypeMetadata theAnnotatedTypeMetadata) {
    String property = theConditionContext.getEnvironment().getProperty("hapi.fhir.ips_enabled");
	  return Boolean.parseBoolean(property);
  }
}
