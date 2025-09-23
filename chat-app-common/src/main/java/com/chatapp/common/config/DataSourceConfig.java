package com.chatapp.common.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration class for setting up read/write data source separation.
 * Configures multiple data sources and routing based on operation type.
 */
@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.write.url}")
    private String writeUrl;

    @Value("${spring.datasource.write.username}")
    private String writeUsername;

    @Value("${spring.datasource.write.password}")
    private String writePassword;

    @Value("${spring.datasource.write.driver-class-name}")
    private String writeDriverClassName;

    @Value("${spring.datasource.read.url}")
    private String readUrl;

    @Value("${spring.datasource.read.username}")
    private String readUsername;

    @Value("${spring.datasource.read.password}")
    private String readPassword;

    @Value("${spring.datasource.read.driver-class-name}")
    private String readDriverClassName;

    /**
     * Creates the write data source for database write operations.
     *
     * @return DataSource configured for write operations
     */
    @Bean(name = "writeDataSource")
    public DataSource writeDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(writeUrl);
        dataSource.setUsername(writeUsername);
        dataSource.setPassword(writePassword);
        dataSource.setDriverClassName(writeDriverClassName);
        return dataSource;
    }

    /**
     * Creates the read data source for database read operations.
     *
     * @return DataSource configured for read operations
     */
    @Bean(name = "readDataSource")
    public DataSource readDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(readUrl);
        dataSource.setUsername(readUsername);
        dataSource.setPassword(readPassword);
        dataSource.setDriverClassName(readDriverClassName);
        return dataSource;
    }

    /**
     * Creates the routing data source that dynamically routes to read or write data sources.
     *
     * @param writeDataSource the data source for write operations
     * @param readDataSource the data source for read operations
     * @return RoutingDataSource that routes based on current context
     */
    @Bean(name = "routingDataSource")
    @Primary
    public DataSource routingDataSource(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource) {

        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put(DataSourceType.WRITE, writeDataSource);
        dataSourceMap.put(DataSourceType.READ, readDataSource);

        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(writeDataSource);

        return routingDataSource;
    }

    /**
     * Creates the JPA entity manager factory with the routing data source.
     *
     * @param dataSource the routing data source
     * @param packagesToScan packages to scan for JPA entities
     * @return LocalContainerEntityManagerFactoryBean configured with routing data source
     */
    @Bean(name = "entityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            @Qualifier("routingDataSource") DataSource dataSource,
            @Value("${spring.jpa.packages-to-scan:}") String packagesToScan) {

        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource);
        em.setPackagesToScan(packagesToScan);

        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);
        properties.put("hibernate.ddl-auto", "none");
        em.setJpaPropertyMap(properties);

        return em;
    }

    /**
     * Creates the JPA transaction manager.
     *
     * @param entityManagerFactory the entity manager factory
     * @return PlatformTransactionManager for handling transactions
     */
    @Bean(name = "transactionManager")
    @Primary
    public PlatformTransactionManager transactionManager(
            @Qualifier("entityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory.getObject());
    }
}