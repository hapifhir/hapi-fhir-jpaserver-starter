package ca.uhn.fhir.jpa.starter;

import static org.assertj.core.api.Assertions.assertThat;

import ca.uhn.fhir.jpa.model.config.SubscriptionSettings;
import org.hl7.fhir.dstu2.model.Subscription.SubscriptionChannelType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.uhn.fhir.jpa.api.config.JpaStorageSettings;
import ca.uhn.fhir.jpa.nickname.INicknameSvc;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {Application.class}, properties = {
		"hapi.fhir.fhir_version=r4",
		"hapi.fhir.mdm_enabled=true"
})
class MdmTest {
	@Autowired
	INicknameSvc nicknameService;
	
	@Autowired
	JpaStorageSettings jpaStorageSettings;

	@Autowired
	SubscriptionSettings subscriptionSettings;

	@Test
	void testApplicationStartedSuccessfully() {
		assertThat(nicknameService).isNotNull();
		assertThat(subscriptionSettings.getSupportedSubscriptionTypes()).contains(SubscriptionChannelType.MESSAGE);
	}
}
