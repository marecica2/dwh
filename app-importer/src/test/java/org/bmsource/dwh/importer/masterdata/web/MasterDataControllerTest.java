package org.bmsource.dwh.importer.masterdata.web;

import org.apache.commons.io.FileUtils;
import org.bmsource.dwh.importer.ImporterApplication;
import org.bmsource.dwh.common.multitenancy.TenantContext;
import org.bmsource.dwh.common.utils.IntegrationTestUtils;
import org.bmsource.dwh.common.utils.TestUtils;
import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.net.URL;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@ActiveProfiles({"integration-test"})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ImporterApplication.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MasterDataControllerTest {
    private static String tenant = "000000-00000-00001";
    private static String project = "1";

    static {
        TenantContext.setTenantSchema(tenant);
    }


    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    JdbcTemplate template;

    @BeforeEach
    public void setup() {
        mockMvc = webAppContextSetup(this.wac).build();
    }

    @Before
    public void before() throws Exception {
        File sql = ResourceUtils.getFile("classpath:batch_clean_up.sql");
        template.execute(FileUtils.readFileToString(sql));
    }

    @AfterAll
    public void afterAll() throws Exception {
        File sql = ResourceUtils.getFile("classpath:batch_clean_up.sql");
        template.execute(FileUtils.readFileToString(sql));
    }

    @Test
    public void testTaxonomyUpload() throws Exception {
        TenantContext.setTenantSchema(tenant);
        URL file = this.getClass().getResource("/taxonomy.xlsx");
        String url = "/taxonomy/import";
        String fileName = "taxonomy.xlsx";
        IntegrationTestUtils.fileUpload(mockMvc, file, url, fileName);
        int importedRows = template.queryForObject("SELECT count(*) FROM \"" + TestUtils.TENANT1 + "\"" +
                ".service_type_taxonomy",
            Integer.class);
        Assertions.assertEquals(34, importedRows);
    }

    @Test
    public void testServiceTypeUpload() throws Exception {
        TenantContext.setTenantSchema(tenant);
        URL file = this.getClass().getResource("/matrix.xlsx");
        String url = "/service-types/import";
        String fileName = "matrix.xlsx";
        IntegrationTestUtils.fileUpload(mockMvc, file, url, fileName);
        int importedRows = template.queryForObject("SELECT count(*) FROM \"" + TestUtils.TENANT1 + "\"" +
            ".service_type_mapping", Integer.class);
        Assertions.assertEquals(56, importedRows);
    }

    @Test
    public void testRateCardUpload() throws Exception {
        TenantContext.setTenantSchema(tenant);
        URL file = this.getClass().getResource("/standard_rate_card_small.xlsx");
        String url = "/rate-cards/import";
        String fileName = "standard_rate_card_small.xlsx";
        IntegrationTestUtils.fileUpload(mockMvc, file, url, fileName);
        int importedRows = template.queryForObject("SELECT count(*) FROM \"" + TestUtils.TENANT1 + "\"" +
            ".standard_rate_card", Integer.class);
        Assertions.assertEquals(0, importedRows);
    }
}
