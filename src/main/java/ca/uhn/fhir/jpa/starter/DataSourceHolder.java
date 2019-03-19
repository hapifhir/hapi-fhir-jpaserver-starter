package ca.uhn.fhir.jpa.starter;

import java.lang.reflect.InvocationTargetException;
import java.sql.Driver;

import org.apache.commons.dbcp2.BasicDataSource;

import ca.uhn.fhir.jpa.starter.tenant.hibernate.MultiTenantConnectionProviderImpl;

/**
 * Hold the dataSource outside of {@link FhirServerConfigCommon}, so that in multi-tenant
 * scenario, {@link MultiTenantConnectionProviderImpl} can have access to it
 */
public enum DataSourceHolder {

	INSTANCE;

	private BasicDataSource dataSource;

	public BasicDataSource getDataSource()
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, ClassNotFoundException {
		if (dataSource == null) {
			dataSource = new BasicDataSource();
			Driver driver = (Driver) Class.forName(HapiProperties.getDataSourceDriver()).getConstructor().newInstance();
			dataSource.setDriver(driver);
			dataSource.setUrl(HapiProperties.getDataSourceUrl());
			dataSource.setUsername(HapiProperties.getDataSourceUsername());
			dataSource.setPassword(HapiProperties.getDataSourcePassword());
			dataSource.setMaxTotal(HapiProperties.getDataSourceMaxPoolSize());
		}
		return dataSource;
	}

}
