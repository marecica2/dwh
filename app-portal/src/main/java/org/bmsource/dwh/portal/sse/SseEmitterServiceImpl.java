package org.bmsource.dwh.portal.sse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;

@Service
public class SseEmitterServiceImpl implements SseEmitterService {
    private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private static final Map<String, List<SseEmitter>> emittersMap = Collections.synchronizedMap(new HashMap<>());

    @Scheduled(fixedRate = 50000)
    public void heartbeat() {
        for (Map.Entry<String, List<SseEmitter>> entry : emittersMap.entrySet()) {
            ListIterator<SseEmitter> iterator = entry.getValue().listIterator();
            while (iterator.hasNext()) {
                SseEmitter emitter = iterator.next();
                try {
                    emitter.send("heartbeat", MediaType.TEXT_PLAIN);
                } catch (Exception e) {
                    logger.trace("Removing closed sse connection " + emitter);
                    emitter.complete();
                    iterator.remove();
                }
            }
        }
        Iterator<Map.Entry<String, List<SseEmitter>>> iter = emittersMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String, List<SseEmitter>> entry = iter.next();
            if (entry.getValue().size() == 0) {
                iter.remove();
            }
        }
        logger.trace("Active sse emmiters {}", emittersMap);
    }

    @Override
    public SseEmitter initSseEmitters(String tenant, String projectId) {
        String key = createKey(tenant, projectId);
        List<SseEmitter> emmiters = emittersMap.get(key);
        if (emmiters == null) {
            emmiters = Collections.synchronizedList(new ArrayList<>());
            emittersMap.put(key, emmiters);
        }
        SseEmitter emitter = new SseEmitter((long) (1000 * 60));
        emitter.onCompletion(() -> emittersMap.get(key).remove(emitter));
        emmiters.add(emitter);
        return emitter;
    }

    private String createKey(String tenant, String projectId) {
        return tenant + ":" + projectId;
    }

    @Override
    public <M> void sendSseEvent(String tenant, String projectId, M message) {
        String key = createKey(tenant, projectId);
        List<SseEmitter> emitters = emittersMap.get(key);
        if (emitters != null) {
            ListIterator<SseEmitter> iterator = emitters.listIterator();
            while (iterator.hasNext()) {
                SseEmitter emitter = iterator.next();
                try {
                    emitter.send(
                        SseEmitter.event().name("appState")
                            .data(message, MediaType.APPLICATION_JSON)
                            .id("appState"));
                } catch (Exception e) {
                    logger.trace("Removing closed sse connection " + emitter);
                    emitter.complete();
                    iterator.remove();
                }
            }
        }
    }
}
