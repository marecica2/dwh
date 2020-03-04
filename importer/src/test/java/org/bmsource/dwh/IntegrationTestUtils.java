package org.bmsource.dwh;

import org.apache.commons.io.FileUtils;
import org.bmsource.dwh.masterdata.ExcelReaderHandler;
import org.bmsource.dwh.masterdata.GenericExcelReader;
import org.bmsource.dwh.masterdata.MasterDataNormalizer;
import org.bmsource.dwh.masterdata.model.RateCard;
import org.bmsource.dwh.masterdata.model.ServiceTypeMapping;
import org.bmsource.dwh.masterdata.model.Taxonomy;
import org.bmsource.dwh.masterdata.repository.RateCardRepository;
import org.bmsource.dwh.masterdata.repository.ServiceTypeMappingRepository;
import org.bmsource.dwh.masterdata.repository.TaxonomyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
@Transactional
public class IntegrationTestUtils {

    @Autowired
    TaxonomyRepository taxonomyRepository;

    @Autowired
    ServiceTypeMappingRepository serviceTypeMappingRepository;

    @Autowired
    RateCardRepository rateCardRepository;

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
            .contentType(mediaType);

        MvcResult resultActions = mockMvc
            .perform(builder)
            .andDo(MockMvcResultHandlers.print())
            .andExpect(request().asyncStarted())
            .andReturn();

        mockMvc.perform(asyncDispatch(resultActions))
            .andExpect(status().is2xxSuccessful());
    }
}
