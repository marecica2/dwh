package org.bmsource.dwh.charts;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface ChartRepository {

    List<Map<String, Object>> queryDistinctValues(String property);

    List<Map<String, Object>> queryAggregate(String projectId,
                                             List<String> measures,
                                             List<String> dimensions,
                                             List<String> sorts,
                                             Map<String, ?> filters);
}
