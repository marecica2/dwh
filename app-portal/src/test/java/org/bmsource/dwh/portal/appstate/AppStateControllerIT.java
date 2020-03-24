package org.bmsource.dwh.portal.appstate;

import org.bmsource.dwh.common.appstate.AppState;
import org.bmsource.dwh.common.appstate.client.EnableAppState;
import org.bmsource.dwh.common.appstate.client.RedisAppStateService;
import org.bmsource.dwh.common.utils.IntegrationTestUtils;
import org.bmsource.dwh.common.utils.TestUtils;
import org.bmsource.dwh.portal.PortalApplication;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.HashMap;
import java.util.Map;

@ActiveProfiles("integration-test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = { PortalApplication.class, IntegrationTestUtils.class }, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EnableAppState
public class AppStateControllerIT {

    @LocalServerPort
    private int port;

    @Autowired
    IntegrationTestUtils testUtils;

    @BeforeAll
    public void setup() {
    }

    @AfterEach
    public void afterEach() {
        testUtils.flushRedis();
    }

    @BeforeEach
    public void beforeEach() {
        testUtils.flushRedis();
    }

    @Autowired
    RedisAppStateService appStateService;


    @Autowired
    ThreadPoolTaskExecutor executor;

    @Test
    public void testAppState() {
        String targetUrl = "http://localhost:" + port + "/api/portal/status?tenant=" +
            TestUtils.TENANT1 +
            "&projectId=1";

        SseTestClient sseClient = new SseTestClient(targetUrl);
        sseClient.connectFeed();
        executor.submit(() -> {
            sleep(10);
            Map<String, Object> state = new HashMap<>();
            state.put("status", "running");
            appStateService.updateState(TestUtils.TENANT1, "1", AppState.STATE_TYPE_IMPORT, state);

            sleep(10);
            state = new HashMap<>();
            state.put("status", "finished");
            appStateService.updateState(TestUtils.TENANT1, "1", AppState.STATE_TYPE_IMPORT, state);

            sleep(10);
            state = new HashMap<>();
            state.put("status", "running");
            appStateService.updateState(TestUtils.TENANT1, "1", AppState.STATE_TYPE_OLAP, state);

            sleep(10);
            state = new HashMap<>();
            state.put("status", "finished");
            appStateService.updateState(TestUtils.TENANT1, "1", AppState.STATE_TYPE_OLAP, state);
        });
        sleep(1000);
        sseClient.getNextEvent();
        sseClient.getNextEvent();
        sseClient.getNextEvent();

        Assertions.assertEquals(4, sseClient.getTotalEventCount());
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
