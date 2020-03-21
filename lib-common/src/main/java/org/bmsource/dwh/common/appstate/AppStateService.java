package org.bmsource.dwh.common.appstate;

import java.util.Map;

public interface AppStateService {
    void updateState(String tenant, String project, String stateType, Map<String, Object> state);
}
