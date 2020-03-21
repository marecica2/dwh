package org.bmsource.dwh.common.appstate;

import org.bmsource.dwh.common.portal.TenantDao;
import org.bmsource.dwh.common.portal.PortalConfiguration;
import org.bmsource.dwh.common.portal.ProjectRepository;
import org.bmsource.dwh.common.portal.Tenant;
import org.bmsource.dwh.common.redis.RedisConfiguration;
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

import java.util.List;

@Configuration
@ComponentScan(basePackageClasses = { PortalConfiguration.class, AppStateConfiguration.class })
@EntityScan(basePackageClasses = { PortalConfiguration.class, AppStateConfiguration.class })
@Import({ RedisConfiguration.class })
public class AppStateConfiguration {

    private Logger logger = LoggerFactory.getLogger(AppStateConfiguration.class);

    @Autowired
    private List<String> channels;

    @Autowired
    private TenantDao tenantDao;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RedisAppStateService appStateService;

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory, MessageListener messageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        for (String topic : channels) {
            for (Tenant tenant : tenantDao.findAll()) {
                for (String project : projectRepository.getProjects(tenant.getSchemaName())) {
                    String topicKey = appStateService.buildTopicKey(tenant.getSchemaName(), project, topic);
                    logger.info("Registering topic channel {}", topicKey);
                    container.addMessageListener(messageListener, new PatternTopic(topicKey));
                }
            }
        }
        return container;
    }
}
