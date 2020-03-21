package org.bmsource.dwh.olap;

import org.bmsource.dwh.common.courier.CourierConfiguration;
import org.bmsource.dwh.common.masterdata.MasterDataConfiguration;
import org.bmsource.dwh.common.multitenancy.EnableMultitenancy;
import org.bmsource.dwh.common.portal.PortalConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(
    exclude = {
        DataSourceAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class,
        SecurityAutoConfiguration.class
    },
    scanBasePackageClasses = {
        OlapApplication.class,
        CourierConfiguration.class,
    }
)
@EnableJpaRepositories(
    basePackageClasses = {
        OlapApplication.class,
        CourierConfiguration.class,
        MasterDataConfiguration.class,
        PortalConfiguration.class,
    }
)
@EntityScan(basePackageClasses = {
    OlapApplication.class,
    CourierConfiguration.class,
    MasterDataConfiguration.class,
    PortalConfiguration.class,
})
@EnableAspectJAutoProxy
@EnableAsync
@EnableTransactionManagement
@EnableMultitenancy(OlapApplication.class)
public class OlapApplication {
    public static void main(String[] args) {
        SpringApplication.run(OlapApplication.class, args);
    }
}
