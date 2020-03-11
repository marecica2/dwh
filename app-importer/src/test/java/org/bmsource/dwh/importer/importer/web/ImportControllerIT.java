package org.bmsource.dwh.importer.importer.web;

import org.apache.commons.io.FileUtils;
import org.bmsource.dwh.importer.ImporterApplication;
import org.bmsource.dwh.common.filemanager.TmpFileManager;
import org.bmsource.dwh.common.multitenancy.TenantContext;
import org.bmsource.dwh.common.utils.IntegrationTestUtils;
import org.bmsource.dwh.common.utils.TestUtils;
import org.bmsource.dwh.common.courier.Fact;
import org.bmsource.dwh.common.courier.FactRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultHandler;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@ActiveProfiles("integration-test")
@SpringBootTest
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { ImporterApplication.class, IntegrationTestUtils.class, TmpFileManager.class })
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ImportControllerIT {
    private static boolean printRest = false;
    private static String tenant = "000000-00000-00001";
    private static String project = "1";

    static {
        TenantContext.setTenantSchema(tenant);
    }

    private List<String> files = new ArrayList<String>() {{
        add("/spends.xlsx");
    }};

    private Map<String, String> mapping = new LinkedHashMap<String, String>() {{
        put("S. No.", "transactionId");
        put("Supplier Name", "supplierName");
        put("Business Unit", "businessUnit");
        put("Origin-City", "originCity");
        put("Origin-State", "originState");
        put("Origin-Country", "originCountry");
        put("Origin-Zip code", "originZip");
        put("Destination-City", "destinationCity");
        put("Destination-State", "destinationState");
        put("Destination-Country", "destinationCountry");
        put("Destination-Zip code", "destinationZip");
        put("Zone", "zone");
        put("Date of Shipment (MM/DD/YYYY)", "shipmentDate");
        put("Date of delivery (MM/DD/YYYY)", "deliveryDate");
        put("Service Type", "serviceType");
        put("Cost (USD)", "cost");
        put("Billable Weight (lb)", "billableWeight");
        put("Actual Weight (lb)", "actualWeight");
        put("Length of package (inches)", "length");
        put("Width of Package (inches)", "width");
        put("Height of Package (inches)", "height");
        put("Discount (%)", "discount");
        put("Distance in Miles", "distance");
    }};

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    JdbcTemplate template;

    @Autowired
    IntegrationTestUtils integrationTestUtils;

    @Autowired
    FactRepository factRepository;

    @BeforeAll
    public void setup() throws Exception {
        mvc = webAppContextSetup(this.wac).build();
        integrationTestUtils.hasRateCards();
        integrationTestUtils.hasTaxonomy();
        integrationTestUtils.hasServiceTypeMapping();
    }

    @AfterAll
    public void after() throws Exception {
        File sql = ResourceUtils.getFile("classpath:batch_clean_up.sql");
        template.execute(FileUtils.readFileToString(sql));

    }

    @Test
    public void testImport() throws Exception {
        MvcResult transactionResult = mvc.perform(MockMvcRequestBuilders
            .post("/import/init")
            .header("x-tenant", tenant)
            .param("projectId", project)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andReturn();
        String transactionId = transactionResult.getResponse().getContentAsString();
        UUID uuid = UUID.fromString(transactionId);
        Assertions.assertNotNull(uuid);

        MockMultipartFile mockMultipartFile = getMultipartFile();
        MediaType mediaType = getMediaType();

        mvc.perform(MockMvcRequestBuilders
            .multipart("/import/{transactionId}", transactionId)
            .file(mockMultipartFile)
            .header("x-tenant", tenant)
            .param("projectId", project)
            .contentType(mediaType))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(content().json(new JSONArray().put(files.get(0)).toString()));

        mvc.perform(MockMvcRequestBuilders
            .post("/import/{transactionId}/mapping", transactionId)
            .header("x-tenant", tenant)
            .param("projectId", project)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceColumns").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceColumns.*", hasSize(38)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.sourceColumns['Supplier Name']").value("UPS"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.destinationColumns").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.destinationColumns.*", hasSize(31)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.destinationColumns.transactionId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$.destinationColumns.transactionId.type").value("String"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.destinationColumns.transactionId.label").value("Transaction " +
                "Id"));

        mvc.perform(MockMvcRequestBuilders
            .post("/import/{transactionId}/preview", transactionId)
            .header("x-tenant", tenant)
            .param("projectId", project)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new JSONObject().put("mapping", new JSONObject(mapping)).toString()))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(100)));

        mvc.perform(MockMvcRequestBuilders
            .post("/import/{transactionId}/start", transactionId)
            .header("x-tenant", tenant)
            .param("projectId", project)
            .contentType(MediaType.APPLICATION_JSON)
            .content(new JSONObject().put("mapping", new JSONObject(mapping)).toString()))
            .andDo(doPrint())
            .andExpect(status().isOk());

        waitUntilCompleted();

        mvc.perform(MockMvcRequestBuilders
            .get("/import/stats")
            .header("x-tenant", tenant)
            .param("projectId", project)
            .contentType(MediaType.APPLICATION_JSON))
            .andDo(doPrint())
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("COMPLETED"));

        TenantContext.setTenantSchema(TestUtils.TENANT1);
        Fact fact = factRepository.findByTransactionId("2").get();
        Assertions.assertEquals(424, factRepository.count());
        Assertions.assertEquals(new BigDecimal("67.73"), fact.getCost());

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
        if (printRest)
            return MockMvcResultHandlers.print();
        return result -> {
        };
    }

    private void waitUntilCompleted() {
        String status = null;
        while (status == null || "STARTED".equals(status)) {
            try {
                Thread.sleep(500);
                MvcResult result = mvc.perform(MockMvcRequestBuilders
                    .get("/import/stats")
                    .header("x-tenant", tenant)
                    .param("projectId", project)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
                status = new JSONObject(result.getResponse().getContentAsString()).getString("status");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private MockMultipartFile getMultipartFile() throws IOException, URISyntaxException {
        URL file = this.getClass().getResource(files.get(0));
        return new MockMultipartFile("file", files.get(0),
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            Files.readAllBytes(Paths.get(file.toURI())));
    }

    private MediaType getMediaType() {
        Map<String, String> contentTypeParams = new HashMap<String, String>();
        contentTypeParams.put("boundary", "------SomeBoundary");
        return new MediaType("multipart", "form-data", contentTypeParams);
    }
}
