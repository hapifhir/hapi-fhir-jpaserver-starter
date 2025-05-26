package ca.uhn.fhir.jpa.starter.util;

import ca.uhn.fhir.context.ConfigurationException;
import ca.uhn.fhir.jpa.config.HibernatePropertiesProvider;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
import org.hibernate.engine.jdbc.dialect.spi.DatabaseMetaDataDialectResolutionInfoAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class JpaHibernatePropertiesProvider extends HibernatePropertiesProvider {

	private static final Logger logger = LoggerFactory.getLogger(JpaHibernatePropertiesProvider.class);
	public static final String PACKAGE_NAME = "ca.uhn.fhir.jpa.model.dialect.HapiFhir";

	private final Dialect dialect;

	public JpaHibernatePropertiesProvider(LocalContainerEntityManagerFactoryBean myEntityManagerFactory) {
		DataSource connection = myEntityManagerFactory.getDataSource();
		Dialect temporaryDialect;
		try {
			assert connection != null;
			try (Connection dbConnection = connection.getConnection()) {
				temporaryDialect = new StandardDialectResolver()
						.resolveDialect(new DatabaseMetaDataDialectResolutionInfoAdapter(dbConnection.getMetaData()));
			}
		} catch (SQLException sqlException) {
			throw new ConfigurationException(sqlException.getMessage(), sqlException);
		}
		try {
			temporaryDialect = (Dialect) Class.forName(
							PACKAGE_NAME.concat(temporaryDialect.getClass().getSimpleName()))
					.getDeclaredConstructor()
					.newInstance();
		} catch (Exception e) {
			logger.error("Looking up optimized HAPI adjusted dialect failed", e);
		}
		dialect = temporaryDialect;
	}

	@Override
	public Dialect getDialect() {
		return dialect;
	}
}
