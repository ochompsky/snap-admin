/* 
 * SnapAdmin - An automatically generated CRUD admin UI for Spring Boot apps
 * Copyright (C) 2023 Ailef (http://ailef.tech)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package tech.ailef.snapadmin.external;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import tech.ailef.snapadmin.internal.InternalSnapAdminConfiguration;

/**
 * The configuration class for "internal" data source. This is not the
 * source connected to the user's data/entities, but rather an internal
 * H2 database which is used by SnapAdmin to store user
 * settings and other information like operations history. 
 */
@ConditionalOnProperty(name = "snapadmin.enabled", matchIfMissing = false)
@ComponentScan
@EnableConfigurationProperties(SnapAdminProperties.class)
@Configuration
@EnableJpaRepositories(
	entityManagerFactoryRef = "internalEntityManagerFactory", 
	basePackages = { "tech.ailef.snapadmin.internal.repository" }
)
@EnableTransactionManagement
@Import(InternalSnapAdminConfiguration.class)
public class SnapAdminAutoConfiguration {
	@Autowired
	private SnapAdminProperties props;
	
	@Value("${internal.datasource.driver-class-name}")
	private String driverClassName;

	@Value("${internal.datasource.url}")
	private String url;

	@Value("${internal.datasource.username}")
	private String username;

	@Value("${internal.datasource.password}")
	private String password;

	@Value("${internal.datasource.hibernate-dialect}")
	private String hibernateDialect;

	@Value("${internal.datasource.hbm2ddl-auto}")
	private String hbm2ddlAuto;


	/**
	 * Builds and returns the internal data source.
	 * 
	 * @return
	 */
	@Bean
	DataSource internalDataSource() {
		DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create();
		dataSourceBuilder.driverClassName(driverClassName);
		dataSourceBuilder.url(url);
		dataSourceBuilder.username(username);
		dataSourceBuilder.password(password);
		return dataSourceBuilder.build();
	}


	@Bean
	LocalContainerEntityManagerFactoryBean internalEntityManagerFactory() {
		LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
		factoryBean.setDataSource(internalDataSource());
		factoryBean.setPersistenceUnitName("internal");
		factoryBean.setPackagesToScan("tech.ailef.snapadmin.internal.model");

		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		factoryBean.setJpaVendorAdapter(vendorAdapter);

		Properties properties = new Properties();
		properties.setProperty("hibernate.dialect", hibernateDialect);
		properties.setProperty("hibernate.hbm2ddl.auto", hbm2ddlAuto);
		factoryBean.setJpaProperties(properties);

		factoryBean.afterPropertiesSet();
		return factoryBean;
	}

	/**
	 * The internal transaction manager. It is not defined as a bean
	 * in order to avoid "colliding" with the default transactionManager
	 * registered by the user. Internally, we use this to instantiate a
	 * TransactionTemplate and run all transactions manually instead of
	 * relying on the @link {@link Transactional} annotation.
	 * @return
	 */
	PlatformTransactionManager internalTransactionManager() {
		JpaTransactionManager transactionManager = new JpaTransactionManager();
		transactionManager.setEntityManagerFactory(internalEntityManagerFactory().getObject());
		return transactionManager;
	}
	
	@Bean
	TransactionTemplate internalTransactionTemplate() {
	    return new TransactionTemplate(internalTransactionManager());
	}

}
