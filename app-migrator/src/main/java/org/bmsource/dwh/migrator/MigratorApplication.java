package org.bmsource.dwh.migrator;

import org.bmsource.dwh.common.portal.PortalConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.rest.RepositoryRestMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.annotation.PostConstruct;

@SpringBootApplication(
    scanBasePackageClasses = {
        MigratorApplication.class,
    },
    exclude = {
        RepositoryRestMvcAutoConfiguration.class
    }
)
@EnableJpaRepositories(
    basePackageClasses = {
        MigratorApplication.class,
        PortalConfiguration.class,
    }
)
@EntityScan(basePackageClasses = {
    MigratorApplication.class,
    PortalConfiguration.class,
})
@EnableTransactionManagement
public class MigratorApplication {

    private static Logger logger = LoggerFactory
        .getLogger(MigratorApplication.class);

    @Autowired
    Migrator migrator;

    @PostConstruct
    public void init() {
        logger.info("Migrating schemas");
        migrator.migrate();
        logger.info("Migration completed");
    }

    public static void main(String[] args) {
        SpringApplication.run(MigratorApplication.class, args);
    }
}
