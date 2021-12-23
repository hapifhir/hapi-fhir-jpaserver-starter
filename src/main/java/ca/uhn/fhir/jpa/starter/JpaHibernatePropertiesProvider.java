package ca.uhn.fhir.jpa.starter;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.jpa.config.HibernatePropertiesProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.sql.SQLException;

public class JpaHibernatePropertiesProvider extends HibernatePropertiesProvider {

  private final Dialect dialect;

  public JpaHibernatePropertiesProvider(LocalContainerEntityManagerFactoryBean myEntityManagerFactory) {
    DataSource connection = myEntityManagerFactory.getDataSource();
    try {
      dialect = new StandardDialectResolver()
        .resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(connection.getConnection().getMetaData()));
    } catch (SQLException sqlException) {
      throw new ConfigurationException(sqlException.getMessage(), sqlException);
    }
  }

  @Override
  public Dialect getDialect() {
    return dialect;
  }
}
