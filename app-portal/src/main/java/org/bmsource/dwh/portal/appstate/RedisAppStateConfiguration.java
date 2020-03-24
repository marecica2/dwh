package org.bmsource.dwh.portal.appstate;

import org.bmsource.dwh.common.appstate.AppState;
import org.bmsource.dwh.common.appstate.client.RedisAppStateService;
import org.bmsource.dwh.common.portal.*;
import org.bmsource.dwh.common.redis.RedisConfiguration;
import org.bmsource.dwh.portal.tenants.TenantRep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan(basePackageClasses = { PortalConfiguration.class, RedisAppStateConfiguration.class })
@EntityScan(basePackageClasses = { PortalConfiguration.class, RedisAppStateConfiguration.class })
@Import({ RedisConfiguration.class })
public class RedisAppStateConfiguration {

    private Logger logger = LoggerFactory.getLogger(RedisAppStateConfiguration.class);

    @Autowired
    private TenantRep tenantRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RedisAppStateService appStateService;

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListener messageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        for (String topic : AppState.TOPICS) {
            for (Tenant tenant : tenantRepository.findAll()) {
                for (String project : projectRepository.getProjects(tenant.getId())) {
                    String topicKey = AppState.buildTopicKey(tenant.getId(), project, topic);
                    logger.debug("Registering topic channel {}", topicKey);
                    container.addMessageListener(messageListener, new PatternTopic(topicKey));
                }
            }
        }
        return container;
    }
}
