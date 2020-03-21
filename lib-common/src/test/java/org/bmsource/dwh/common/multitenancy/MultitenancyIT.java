package org.bmsource.dwh.common.multitenancy;

import org.apache.commons.io.FileUtils;
import org.bmsource.dwh.common.multitenancy.app.TestApplication;
import org.bmsource.dwh.common.multitenancy.impl.MultitenancyConfiguration;
import org.bmsource.dwh.common.multitenancy.impl.MultitenantDataSource;
import org.bmsource.dwh.common.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestApplication.class, MultitenancyConfiguration.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("integration-test-pg")
public class MultitenancyIT {
    private boolean printRest = false;
    private String project = "1";
    private MockMvc mvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    JdbcTemplate template;

    @BeforeAll
    public void setup() throws IOException {
        mvc = webAppContextSetup(this.wac).build();
        File sql = ResourceUtils.getFile("classpath:multitenancy-before-script.sql");
        template.execute(FileUtils.readFileToString(sql));
    }

    @Test
    public void testForTenant1() throws Exception {
        mvc.perform(MockMvcRequestBuilders
            .get("/test")
            .header(Constants.TENANT_HEADER, TestUtils.TENANT1)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("John"));
    }

    @Test
    public void testForTenant2() throws Exception {
        mvc.perform(MockMvcRequestBuilders
            .get("/test")
            .header(Constants.TENANT_HEADER, TestUtils.TENANT2)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("Mary"));
    }

    @Test
    public void testForUnknownTenant() throws Exception {
        mvc.perform(MockMvcRequestBuilders
            .get("/test")
            .header(Constants.TENANT_HEADER, "foo")
            .param("projectId", project)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().is4xxClientError());
    }

    private ResultHandler doPrint() {
        if (printRest)
            return MockMvcResultHandlers.print();
        return result -> {
        };
    }

}
