package org.bmsource.dwh.portal.appstate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SseTestClient {
    private static final Logger logger = LoggerFactory.getLogger(SseTestClient.class);
    private final String targetUrl;
    private final List<InboundEvent> receivedEvents = Collections.synchronizedList(new ArrayList<>());

    private String clientName = "SSE client";
    private EventSource eventSource;
    private int currentEventPos = 0;

    public SseTestClient( String targetUrl ) {
        this.targetUrl = targetUrl;
    }

    public SseTestClient setClientName( String clientName ) {
        this.clientName = clientName;
        return this;
    }

    public SseTestClient connectFeed() {
        logger.info("[{}] Initializing EventSource...", clientName);

        Client client = ClientBuilder
            .newBuilder()
            .register(SseFeature.class)
            .build();

        eventSource = EventSource
            .target(client.target(targetUrl))
            .reconnectingEvery(300, TimeUnit.SECONDS)
            .build();

        EventListener listener = inboundEvent -> {
            logger.info("[{}] Event received: name -> {}, data -> {}",
                clientName, inboundEvent.getName(), inboundEvent.readData(String.class));
            receivedEvents.add(inboundEvent);
        };
        eventSource.register(listener);
        eventSource.open();
        logger.info("[{}] EventSource connection opened", clientName);
        return this;
    }

    public int getTotalEventCount() {
        return receivedEvents.size();
    }

    public int getNewEventCount() {
        return receivedEvents.size() - currentEventPos;
    }

    public boolean hasNewEvent() {
        return currentEventPos < receivedEvents.size();
    }

    public InboundEvent getNextEvent() {
        if (currentEventPos >= receivedEvents.size()) {
            return null;
        }
        InboundEvent currentEvent = receivedEvents.get(currentEventPos);
        ++currentEventPos;
        return currentEvent;
    }

    public SseTestClient closeFeed() {
        if (eventSource != null) {
            eventSource.close();
        }
        return this;
    }
}
