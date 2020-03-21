package org.bmsource.dwh.common.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JdbcJobRepository {

    @Autowired
    JdbcTemplate template;

    public Long getLastJobExecutionId(String tenant, String project) {
        String sql = "select job_execution_id\n" +
            "from batch_job_execution_params\n" +
            "         join batch_job_execution bje using (job_execution_id)\n" +
            "         join batch_job_instance bji using (job_instance_id)\n" +
            "where key_name = '" + JobConstants.tenantProjectKey + "'\n" +
            "  and string_val = ?\n" +
            "  and bji.job_name = '" + JobConstants.jobName + "' " + "\n" +
            "order by job_execution_id desc\n" +
            "limit 1";
        List<Long> res = template.queryForList(sql, Long.class, tenant + ":" + project);
        if (res.isEmpty())
            return null;
        return res.get(0);
    }
}
