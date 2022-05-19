package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.jpa.subscription.match.registry.SubscriptionLoader;
import ca.uhn.fhir.jpa.subscription.match.registry.SubscriptionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import javax.servlet.ServletException;

@Import(AppProperties.class)
public class JpaRestfulServer extends BaseJpaRestfulServer {

  @Autowired
  AppProperties appProperties;
  @Autowired
  SubscriptionLoader mySubscriptionLoader;

  private static final long serialVersionUID = 1L;

  public JpaRestfulServer() {
    super();
  }

  @Override
  protected void initialize() throws ServletException {
    super.initialize();
    mySubscriptionLoader.syncSubscriptions();
    // Add your own customization here

  }

}
