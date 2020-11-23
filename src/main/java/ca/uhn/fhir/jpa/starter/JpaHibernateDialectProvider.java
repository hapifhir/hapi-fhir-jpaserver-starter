package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.jpa.config.HibernateDialectProvider;
import org.hibernate.dialect.Dialect;
import org.springframework.beans.factory.annotation.Value;

public class JpaHibernateDialectProvider extends HibernateDialectProvider {

  @Value("${spring.jpa.properties.hibernate.dialect}")
  public String myDialectClass;


  @Override
  public Dialect getDialect() {
    try {
      return (Dialect) Class.forName(myDialectClass).newInstance();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
      throw new ConfigurationException("Can not load dialect: " + myDialectClass);
    }
  }
}
