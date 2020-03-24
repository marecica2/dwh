package org.bmsource.dwh.olap.charts;

import org.bmsource.dwh.common.multitenancy.TenantContext;
import org.bmsource.dwh.common.utils.IntegrationTestUtils;
import org.bmsource.dwh.olap.OlapApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles("integration-test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {OlapApplication.class, IntegrationTestUtils.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChartControllerIT {
    private boolean printRest = false;
    private static String tenant = "000000-00000-00001";
    private static String project = "1";

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    JdbcTemplate template;

    @Autowired
    IntegrationTestUtils testUtils;

    @BeforeEach
    public void setup() {
        mvc = webAppContextSetup(this.wac).build();
    }

    @AfterEach
    public void afterEach() {
        testUtils.flushRedis();
    }

    @Test
    @Sql(scripts = "/spends.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "/clean-up.sql", executionPhase = AFTER_TEST_METHOD)
    public void testChart() throws Exception {
        String[] measures = new String[] {"sumCost"};
        String[] dimensions = new String[] {"supplierName"};
        String[] sorts = new String[] {"ascSumCost"};
        mvc.perform(MockMvcRequestBuilders
            .get("/charts")
            .header("x-tenant", tenant)
            .param("projectId", project)
            .param("measures", measures)
            .param("dimensions", dimensions)
            .param("sorts", sorts)
            .param("supplierName", "Ups,Fedex")
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].supplierName").value("Fedex"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].sumCost").value(307.32))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].supplierName").value("Ups"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[1].sumCost").value(1987.52));
    }

    @Test
    @Sql(scripts = "/spends.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "/clean-up.sql", executionPhase = AFTER_TEST_METHOD)
    public void testDimensions() throws Exception {
        String dimension = "supplierName";
        mvc.perform(MockMvcRequestBuilders
            .get("/charts/dimensions")
            .header("x-tenant", tenant)
            .param("projectId", project)
            .param("dimension", dimension)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.*", hasSize(2)))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].supplierName").value("Fedex"));
    }

    @Test
    @Sql(scripts = "/spends.sql", executionPhase = BEFORE_TEST_METHOD)
    @Sql(scripts = "/clean-up.sql", executionPhase = AFTER_TEST_METHOD)
    public void testFactsPreview() throws Exception {
        TenantContext.setTenantSchema(tenant);
        mvc.perform(MockMvcRequestBuilders
            .get("/facts?page={page}&size={size}", 0, 1)
            .header("x-tenant", tenant)
            .param("projectId", project)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$._embedded.facts").exists());
    }

    private ResultHandler doPrint() {
        if(printRest)
            return MockMvcResultHandlers.print();
        return result -> { };
    }

}
