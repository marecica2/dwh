package org.bmsource.dwh.common.utils;

import org.apache.commons.io.FileUtils;
import org.bmsource.dwh.common.excel.reader.ExcelReaderHandler;
import org.bmsource.dwh.common.excel.reader.GenericExcelReader;
import org.bmsource.dwh.common.masterdata.MasterDataNormalizer;
import org.bmsource.dwh.common.masterdata.model.RateCard;
import org.bmsource.dwh.common.masterdata.model.ServiceTypeMapping;
import org.bmsource.dwh.common.masterdata.model.Taxonomy;
import org.bmsource.dwh.common.masterdata.repository.RateCardRepository;
import org.bmsource.dwh.common.masterdata.repository.ServiceTypeMappingRepository;
import org.bmsource.dwh.common.masterdata.repository.TaxonomyRepository;
import org.bmsource.dwh.common.multitenancy.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
@Transactional
public class IntegrationTestUtils {

    @Autowired(required = false)
    TaxonomyRepository taxonomyRepository;

    @Autowired(required = false)
    ServiceTypeMappingRepository serviceTypeMappingRepository;

    @Autowired(required = false)
    RateCardRepository rateCardRepository;

    @Autowired(required = false)
    RedisOperations<String, String> operations;

    public void hasTaxonomy(String... optFileName) throws Exception {
        File file = getResource("taxonomy.xlsx", optFileName);
        importExcel(FileUtils.openInputStream(file), taxonomyRepository, Taxonomy.class,
            MasterDataNormalizer.normalizeTaxonomy, taxonomyRepository::delete);
    }

    public void hasServiceTypeMapping(String... optFileName) throws Exception {
        File file = getResource("matrix.xlsx", optFileName);
        importExcel(FileUtils.openInputStream(file), serviceTypeMappingRepository, ServiceTypeMapping.class,
            MasterDataNormalizer.normalizeServiceTypeMapping, serviceTypeMappingRepository::delete);
    }

    public void hasRateCards(String... optFileName) throws Exception {
        File file = getResource("standard_rate_card_small.xlsx", optFileName);
        importExcel(FileUtils.openInputStream(file), rateCardRepository, RateCard.class,
            MasterDataNormalizer.normalizeRateCards, rateCardRepository::delete);
    }

    public void flushRedis() {
        operations.execute((RedisCallback<Void>) connection -> {
            connection.serverCommands().flushAll();
            return null;
        });
    }

    private <Type, Repository extends JpaRepository<Type, ?>> void importExcel(
        InputStream inputStream,
        Repository repository,
        Class<Type> classType,
        Function<Type, Type> transform,
        Runnable deleteFn
    ) {
        ExcelReaderHandler<Type> handler = new ExcelReaderHandler<Type>() {
            @Override
            public void onStart() {
                deleteFn.run();
            }

            @Override
            public void onRead(List<Type> items) {
                try {
                    List<Type> itemsTransformed = items
                        .stream()
                        .map(this::transform)
                        .collect(Collectors.toList());
                    repository.saveAll(itemsTransformed);
                    repository.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public Type transform(Type item) {
                return transform.apply(item);
            }
        };

        GenericExcelReader<Type> excelParser = new GenericExcelReader<>(handler, classType);
        try {
            excelParser.parse(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getResource(String defaultFile, String[] optFileName) throws FileNotFoundException {
        File file = ResourceUtils.getFile("classpath:" + defaultFile);
        if (optFileName != null && optFileName.length > 0)
            file = ResourceUtils.getFile("classpath:" + optFileName[0]);
        return file;
    }

    public static void fileUpload(MockMvc mockMvc, URL file, String url, String fileName) throws Exception {
        MockMultipartFile mockMultipartFile = new MockMultipartFile("file", fileName,
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", Files.readAllBytes(Paths.get(file.toURI())));

        Map<String, String> contentTypeParams = new HashMap<String, String>();
        contentTypeParams.put("boundary", "------SomeBoundary");
        MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

        MockHttpServletRequestBuilder builder = MockMvcRequestBuilders
            .multipart(url)
            .file(mockMultipartFile)
            .header(Constants.TENANT_HEADER, TestUtils.TENANT1)
            .contentType(mediaType);

        MvcResult resultActions = mockMvc
            .perform(builder)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(MockMvcRequestBuilders.asyncDispatch(resultActions))
            .andExpect(status().is2xxSuccessful());
    }
}
