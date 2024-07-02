package be.fgov.ehealth.repository;


import be.fgov.ehealth.entities.Tenants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import com.zaxxer.hikari.HikariDataSource;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.AbstractJpaVendorAdapter;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "testserverEntityManager",
	transactionManagerRef = "testserverTransactionManager", basePackageClasses = Tenants.class)
public class TestServerConfig implements ApplicationContextAware {

	private final PersistenceUnitManager persistenceUnitManager;
	private ApplicationContext context;

	public TestServerConfig(ObjectProvider<PersistenceUnitManager> persistenceUnitManager) {
		this.persistenceUnitManager = persistenceUnitManager.getIfAvailable();
	}

	@Bean
	@ConfigurationProperties("spring.testserver.jpa")
	public JpaProperties testserverJpaProperties() {
		return new JpaProperties();
	}

	@Bean
	@ConfigurationProperties("spring.testserver.datasource")
	public DataSourceProperties testserverDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.testserver.datasource.properties")
	public HikariDataSource testserverDataSource() {
		return testserverDataSourceProperties().initializeDataSourceBuilder().type(HikariDataSource.class).build();
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean testserverEntityManager(@Qualifier("testserverJpaProperties") JpaProperties testserverJpaProperties) {
		EntityManagerFactoryBuilder builder = createEntityManagerFactoryBuilder(testserverJpaProperties);
		return builder.dataSource(testserverDataSource()).packages(Tenants.class).persistenceUnit("testserverDs").build();
	}

	@Bean
	public JpaTransactionManager testserverTransactionManager(@Qualifier("testserverEntityManager") EntityManagerFactory testserverEntityManager) {
		return new JpaTransactionManager(testserverEntityManager);
	}

	private EntityManagerFactoryBuilder createEntityManagerFactoryBuilder(@Qualifier("testserverJpaProperties") JpaProperties testserverJpaProperties) {
		JpaVendorAdapter jpaVendorAdapter = createJpaVendorAdapter(testserverJpaProperties);
		return new EntityManagerFactoryBuilder(jpaVendorAdapter, testserverJpaProperties.getProperties(),
			this.persistenceUnitManager);
	}

	private JpaVendorAdapter createJpaVendorAdapter(@Qualifier("testserverJpaProperties") JpaProperties jpaProperties) {
		AbstractJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
		adapter.setShowSql(jpaProperties.isShowSql());
		if (jpaProperties.getDatabase() != null) {
			adapter.setDatabase(jpaProperties.getDatabase());
		}
		if (jpaProperties.getDatabasePlatform() != null) {
			adapter.setDatabasePlatform(jpaProperties.getDatabasePlatform());
		}
		adapter.setGenerateDdl(jpaProperties.isGenerateDdl());
		return adapter;
	}

	@Bean
	@Primary
	JpaProperties getJpaProperties(){
		return context.getBean("spring.jpa-org.springframework.boot.autoconfigure.orm.jpa.JpaProperties", JpaProperties.class);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.context = applicationContext;
	}
}
