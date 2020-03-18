package org.bmsource.dwh.common.portal;

import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class ProjectRepository {

    public List<String> getProjects(String tenant) {
        return Arrays.asList("1");
    }
}
