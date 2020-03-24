package org.bmsource.dwh.portal.appstate;

import org.bmsource.dwh.common.appstate.AppState;
import org.bmsource.dwh.portal.sse.SseEmitterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AppStateListener implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(AppStateListener.class);
    private final SseEmitterService sseEmitter;
    private final RedisTemplate<String, Object> template;

    public AppStateListener(SseEmitterService sseEmitter, RedisTemplate<String, Object> template) {
        this.template = template;
        this.sseEmitter = sseEmitter;
    }

    public void onMessage(Message message, byte[] pattern) {
        String[] params = new String(message.getBody()).split(":");
        String tenant = params[0];
        String project = params[1];
        Map<String, Map<String, Object>> state = getState(tenant, project);
        logger.debug("Emitting state for tenant: {} project: {} topic: {}, state: {}",
            tenant, project, new String(pattern), state);
        this.sseEmitter.sendSseEvent(tenant, project, state);
    }

    private Map<String, Map<String, Object>> getState(String tenant, String project) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        String key = AppState.buildKeyPrefix(tenant, project).append("*").toString();
        Set<String> keys = template.keys(key);
        for (String entryKey : keys) {
            String[] keyVals = AppState.parseKey(entryKey);
            Map<String, Object> entry = template.<String, Object>opsForHash().entries(entryKey);
            result.put(keyVals[1], entry);
        }
        return result;
    }
}
