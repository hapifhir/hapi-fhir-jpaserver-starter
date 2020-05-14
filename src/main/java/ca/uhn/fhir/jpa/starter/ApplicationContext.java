package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.FhirVersionEnum;
import ca.uhn.fhir.jpa.subscription.channel.config.SubscriptionChannelConfig;
import ca.uhn.fhir.jpa.subscription.match.config.SubscriptionProcessorConfig;
import ca.uhn.fhir.jpa.subscription.match.config.WebsocketDispatcherConfig;
import ca.uhn.fhir.jpa.subscription.submit.config.SubscriptionSubmitterConfig;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class ApplicationContext extends AnnotationConfigWebApplicationContext {

  public ApplicationContext() {
    FhirVersionEnum fhirVersion = HapiProperties.getFhirVersion();
    if (fhirVersion == FhirVersionEnum.DSTU2) {
      register(FhirServerConfigDstu2.class, FhirServerConfigCommon.class);
    } else if (fhirVersion == FhirVersionEnum.DSTU3) {
      register(FhirServerConfigDstu3.class, FhirServerConfigCommon.class);
    } else if (fhirVersion == FhirVersionEnum.R4) {
      register(FhirServerConfigR4.class, FhirServerConfigCommon.class);
    } else if (fhirVersion == FhirVersionEnum.R5) {
      register(FhirServerConfigR5.class, FhirServerConfigCommon.class);
    } else {
      throw new IllegalStateException();
    }

    if (HapiProperties.getSubscriptionWebsocketEnabled()) {
      register(WebsocketDispatcherConfig.class);
    }

    if (HapiProperties.getSubscriptionEmailEnabled()
    || HapiProperties.getSubscriptionRestHookEnabled()
    || HapiProperties.getSubscriptionWebsocketEnabled()) {
      register(SubscriptionSubmitterConfig.class);
      register(SubscriptionProcessorConfig.class);
      register(SubscriptionChannelConfig.class);
    }

  }

}
