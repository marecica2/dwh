package org.bmsource.dwh.portal;

import org.bmsource.dwh.common.appstate.client.EnableAppState;
import org.bmsource.dwh.common.security.client.ClientSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaRepositories
@EntityScan
@EnableAsync
@EnableAppState
@Import({
    ClientSecurityConfig.class
})
public class PortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PortalApplication.class, args);
    }
}
