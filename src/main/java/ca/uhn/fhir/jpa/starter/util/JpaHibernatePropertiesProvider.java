package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.jpa.config.HibernatePropertiesProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JpaHibernatePropertiesProvider extends HibernatePropertiesProvider {

	private final Dialect dialect;

	public JpaHibernatePropertiesProvider(LocalContainerEntityManagerFactoryBean myEntityManagerFactory) {
		DataSource connection = myEntityManagerFactory.getDataSource();
		try (Connection dbConnection = connection.getConnection()) {
			dialect = new StandardDialectResolver()
					.resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(dbConnection.getMetaData()));
		} catch (SQLException sqlException) {
			throw new ConfigurationException(sqlException.getMessage(), sqlException);
		}
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}
}
