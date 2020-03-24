package org.bmsource.dwh.common.appstate;

import java.util.ArrayList;
import java.util.List;

public class AppState {

    public static final String APP_STATE_TOPIC = "appState";

    public static final String STATE_TYPE_IMPORT = "importState";
    public static final String STATE_TYPE_OLAP = "olapState";

    public static List<String> TOPICS = new ArrayList<String>() {{
        add(APP_STATE_TOPIC);
    }};

    public static String buildStateKey(String state, String tenant, String project) {
        return buildKeyPrefix(tenant, project)
            .append("&state=")
            .append(state)
            .toString();
    }

    public static String buildTopicKey(String tenant, String project, String topic) {
        return buildKeyPrefix(tenant, project)
            .append("&topic=")
            .append(topic)
            .toString();
    }

    public static StringBuilder buildKeyPrefix(String tenant, String project) {
        return new StringBuilder()
            .append("tenant=").append(tenant)
            .append("&project=").append(project);
    }

    public static String[] parseKey(String entryKey) {
        String[] tokens = entryKey.split("&");
        return tokens[tokens.length - 1].split("=");
    }
}
