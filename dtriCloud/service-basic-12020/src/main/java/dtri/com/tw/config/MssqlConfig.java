package dtri.com.tw.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import jakarta.persistence.EntityManagerFactory;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(//
		entityManagerFactoryRef = "mssqlEntityManagerFactory", //
		transactionManagerRef = "mssqlTransactionManager", //
		basePackages = { "dtri.com.tw.mssql.dao" })
public class MssqlConfig {
	@Bean(name = "mssqlProperties")
	@ConfigurationProperties("mssql.datasource")
	public DataSourceProperties dataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean(name = "mssqlDatasource")
	@ConfigurationProperties(prefix = "mssql.datasource")
	public DataSource datasource(@Qualifier("mssqlProperties") DataSourceProperties properties) {
		return properties.initializeDataSourceBuilder().build();
	}

	@Bean(name = "mssqlEntityManagerFactory")
	public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(EntityManagerFactoryBuilder builder,
			@Qualifier("mssqlDatasource") DataSource dataSource) {

		return builder.dataSource(dataSource)//
				.packages("dtri.com.tw.mssql.entity").persistenceUnit("mssql").build();
	}

	@Bean(name = "mssqlTransactionManager")
	@ConfigurationProperties("spring.jpa")
	public PlatformTransactionManager transactionManager(@Qualifier("mssqlEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
		return new JpaTransactionManager(entityManagerFactory);
	}
}
