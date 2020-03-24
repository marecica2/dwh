package org.bmsource.dwh.common.appstate.client;

import org.bmsource.dwh.common.appstate.AppState;
import org.bmsource.dwh.common.redis.RedisConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Import({ RedisConfiguration.class })
public class RedisAppStateService implements AppStateService {

    @Autowired
    RedisTemplate<String, Object> template;

    @Override
    public void updateState(String tenant, String project, String stateType, Map<String, Object> state) {
        String appStateKey = AppState.buildStateKey(stateType, tenant, project);
        String topicKey = AppState.buildTopicKey(tenant, project, AppState.APP_STATE_TOPIC);
        template.opsForHash().putAll(appStateKey, state);
        template.expire(appStateKey, 30000, TimeUnit.SECONDS);
        template.convertAndSend(topicKey, tenant + ":" + project);
    }
}
