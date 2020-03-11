package org.bmsource.dwh.importer;

import org.bmsource.dwh.common.courier.CourierConfiguration;
import org.bmsource.dwh.common.job.EnableImportJob;
import org.bmsource.dwh.common.multitenancy.impl.concurrent.ContextAwarePoolExecutor;
import org.bmsource.dwh.common.security.client.ClientSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@Import({
    CourierConfiguration.class,
    ClientSecurityConfig.class
})
@EnableImportJob
public class ImporterAppConfiguration {

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ContextAwarePoolExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("importerAsyncThread-");
        executor.initialize();
        return executor;
    }
}
