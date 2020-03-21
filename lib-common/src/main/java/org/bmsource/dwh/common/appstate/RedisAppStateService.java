package org.bmsource.dwh.common.appstate;

import org.bmsource.dwh.common.appstate.pushnotification.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Component
public class RedisAppStateService implements AppStateService, MessageListener {

    private static final String APP_STATE_CHANNEL = "appState";

    @Autowired
    NotificationService notificationService;

    @Autowired
    RedisTemplate<String, Object> template;

    private String buildStateKey(String channel, String tenant, String project) {
        return buildKeyPrefix(tenant, project)
            .append(":channel#")
            .append(channel)
            .toString();
    }

    String buildTopicKey(String tenant, String project, String topic) {
        return buildKeyPrefix(tenant, project)
            .append(":topic#")
            .append(topic)
            .toString();
    }

    private StringBuilder buildKeyPrefix(String tenant, String project) {
        return new StringBuilder()
            .append(APP_STATE_CHANNEL)
            .append(":tenant#").append(tenant)
            .append(":project#").append(project);
    }

    @Override
    public void updateState(String tenant, String project, String stateType, Map<String, Object> state) {
//        String stateKey = buildStateKey(stateType, tenant, project);
//        String topic = buildTopicKey(tenant, project, stateType);
//        template.opsForHash().putAll(stateKey, state);
//        template.expire(stateKey, 10, TimeUnit.SECONDS);
//        template.convertAndSend(topic, tenant + ":" + project);
        notificationService.sendSseEvent(tenant, project, state);
    }

    Map<String, Map<String, Object>> getState(String tenant, String project) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        String key = buildKeyPrefix(tenant, project).append("*").toString();
        Set<String> keys = template.keys(key);
        for (String entryKey : keys) {
            String[] tokens = entryKey.split(":");
            String[] keyVals = tokens[tokens.length - 1].split("#");
            Map<String, Object> entry = template.<String, Object>opsForHash().entries(entryKey);
            result.put(keyVals[1], entry);
        }
        return result;
    }

    @Override
    public void onMessage(final Message message, final byte[] pattern) {
//        String[] params = new String(message.getBody()).split(":");
//        String tenant = params[0];
//        String project = params[1];
//        Map<String, Map<String, Object>> state = getState(tenant, project);
//        notificationService.sendSseEvent(tenant, project, state);
    }

}
