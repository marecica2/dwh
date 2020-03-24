package org.bmsource.dwh.portal.sse;

import org.bmsource.dwh.common.utils.TestUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.mockito.Mockito.any;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SseEmitterServiceImpl.class)
public class SseEmitterImplTest {

    @Test
    public void testHeartbeat() {
        SseEmitterServiceImpl emitter = new SseEmitterServiceImpl();
        emitter.initSseEmitters(TestUtils.TENANT1, "1");
        emitter.initSseEmitters(TestUtils.TENANT2, "1");
        emitter.heartbeat();
        Assertions.assertEquals(2, SseEmitterServiceImpl.emittersMap.size());
    }

    @Test
    public void testHeartbeatWhenDisconnected() throws Exception {
        SseEmitter sseEmitter = PowerMockito.mock(SseEmitter.class);
        PowerMockito.whenNew(SseEmitter.class).withParameterTypes(Long.class).withArguments((long) (1000 * 60)).thenReturn(sseEmitter);

        PowerMockito.doThrow(new IOException()).when(sseEmitter).send(any(Object.class), any(MediaType.class));

        SseEmitterServiceImpl emitter = new SseEmitterServiceImpl();
        emitter.initSseEmitters(TestUtils.TENANT1, "1");
        emitter.heartbeat();
    }
}
