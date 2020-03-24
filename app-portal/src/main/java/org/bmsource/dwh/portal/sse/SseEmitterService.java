package org.bmsource.dwh.portal.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface SseEmitterService {
    SseEmitter initSseEmitters(String tenant, String projectId);

    <M extends Object> void sendSseEvent(String tenant, String projectId, M message);
}
