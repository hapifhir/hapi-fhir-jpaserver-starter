package ca.uhn.fhir.jpa.starter.annotations;

import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.context.annotation.Conditional;

public class OnEitherVersion extends AnyNestedCondition {

	OnEitherVersion() {
		super(ConfigurationPhase.REGISTER_BEAN);
	}

	@Override
	protected ConditionOutcome getFinalMatchOutcome(MemberMatchOutcomes memberOutcomes) {
		ConditionOutcome result = super.getFinalMatchOutcome(memberOutcomes);
		return result;
	}

	@Conditional(OnDSTU2Condition.class)
	static class OnDSTU2 {}

	@Conditional(OnDSTU3Condition.class)
	static class OnDSTU3 {}

	@Conditional(OnR4Condition.class)
	static class OnR4 {}

	@Conditional(OnR4BCondition.class)
	static class OnR4B {}

	@Conditional(OnR5Condition.class)
	static class OnR5 {}
}
