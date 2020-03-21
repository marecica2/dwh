package org.bmsource.dwh.common.multitenancy.impl;

import org.bmsource.dwh.common.multitenancy.EnableMultitenancy;
import org.bmsource.dwh.common.multitenancy.impl.concurrent.ContextAwarePoolExecutor;
import org.bmsource.dwh.common.portal.Tenant;
import org.bmsource.dwh.common.portal.TenantDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.MappedInterceptor;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultitenancyConfiguration implements WebMvcConfigurer {

    @Autowired
    TenantContextInterceptor tenantInterceptor;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSourceProperties properties;

    @Autowired
    @Lazy
    private TenantDao tenantDao;

    @Autowired
    JpaProperties props;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor);
    }

    @Bean
    public MappedInterceptor repositoryRestResourcesInterceptor() {
        return new MappedInterceptor(new String[]{ "/**" }, tenantInterceptor);
    }

    @Bean(name = "multitenantDatasource")
    public DataSource multitenantDataSource() {
        Map<Object, Object> resolvedDataSources = new HashMap<>();
        for (Tenant tenant : tenantDao.findAll()) {
            resolvedDataSources.put(tenant.getSchemaName(), createDatasource(tenant.getSchemaName()));
        }

        MultitenantDataSource dataSource = new MultitenantDataSource();
        dataSource.setDefaultTargetDataSource(createDatasource(null));
        dataSource.setTargetDataSources(resolvedDataSources);
        dataSource.afterPropertiesSet();
        return dataSource;
    }

    @Primary
    @Bean
    public JdbcTemplate jdbcTemplate(@Qualifier("multitenantDatasource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

//    @Bean
//    public EntityManagerFactory entityManagerFactory() {
//        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
//        emf.setDataSource(multitenantDataSource());
//        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
//        emf.setPackagesToScan("org.bmsource.dwh.**");
//        emf.setPersistenceUnitName("default");
//        emf.getJpaPropertyMap().putAll(props.getProperties());
//        emf.afterPropertiesSet();
//        return emf.getObject();
//    }

    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        return new ContextAwarePoolExecutor();
    }

    private DataSource createDatasource(String tenantSchema) {
        DataSourceBuilder<?> dataSourceBuilder = DataSourceBuilder.create(this.getClass().getClassLoader())
            .driverClassName(properties.getDriverClassName())
            .url(resolveTenantConnectionUrl(tenantSchema))
            .username(properties.getUsername())
            .password(properties.getPassword());
        if (properties.getType() != null) {
            dataSourceBuilder.type(properties.getType());
        }
        return dataSourceBuilder.build();
    }

    private String resolvePackageName() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(EnableMultitenancy.class);
        if (beans.size() == 0) {
            throw new RuntimeException("No configuration annotated with " + EnableMultitenancy.class.getName());
        }
        return beans.values().iterator().next().getClass().getPackage().getName();
    }

    private String resolveTenantConnectionUrl(String tenantSchema) {
        String join = properties.getUrl().contains("?") ? "&" : "?";
        return tenantSchema != null ?
            properties.getUrl() + join + "currentSchema=" + tenantSchema :
            properties.getUrl();
    }
}
