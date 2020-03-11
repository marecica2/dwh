package org.bmsource.dwh.importer.masterdata;

import org.bmsource.dwh.common.io.reader.ExcelReaderHandler;
import org.bmsource.dwh.common.io.reader.GenericExcelReader;
import org.bmsource.dwh.common.masterdata.MasterDataNormalizer;
import org.bmsource.dwh.common.masterdata.model.RateCard;
import org.bmsource.dwh.common.masterdata.model.ServiceTypeMapping;
import org.bmsource.dwh.common.masterdata.model.Taxonomy;
import org.bmsource.dwh.common.masterdata.model.ZipCodeLocation;
import org.bmsource.dwh.common.masterdata.repository.RateCardRepository;
import org.bmsource.dwh.common.masterdata.repository.ServiceTypeMappingRepository;
import org.bmsource.dwh.common.masterdata.repository.TaxonomyRepository;
import org.bmsource.dwh.common.masterdata.repository.ZipCodeLocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.InputStream;
import java.util.List;

@RestController
public class MasterDataController {

    @Autowired
    ZipCodeLocationRepository zipCodeRepository;

    @Autowired
    TaxonomyRepository taxonomyRepository;

    @Autowired
    RateCardRepository rateCardRepository;

    @Autowired
    ServiceTypeMappingRepository serviceTypeMappingRepository;

    @PostMapping(value = "zip-code-locations/import")
    public DeferredResult<ResponseEntity<?>> importZipCodes(MultipartHttpServletRequest request) {
        Class<ZipCodeLocation> classType = ZipCodeLocation.class;
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        ExcelReaderHandler<ZipCodeLocation> handler = new ExcelReaderHandler<ZipCodeLocation>() {
            @Override
            public void onStart() {
                zipCodeRepository.delete();
            }

            @Override
            public void onRead(List<ZipCodeLocation> items) {
                try {
                    zipCodeRepository.saveAll(items);
                } catch (Exception e) {
                    new ResponseEntity<Integer>(HttpStatus.INTERNAL_SERVER_ERROR);
                    throw e;
                }
            }

            @Override
            public ZipCodeLocation transform(ZipCodeLocation item) {
                return item;
            }

            @Override
            public void onFinish(int rowCount) {
                result.setResult(new ResponseEntity<Integer>(HttpStatus.CREATED));
            }
        };
        return importExcel(request, classType, handler, result);
    }

    @PostMapping(value = "service-types/import")
    public DeferredResult<ResponseEntity<?>> importServiceTypes(MultipartHttpServletRequest request) {
        Class<ServiceTypeMapping> classType = ServiceTypeMapping.class;
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        ExcelReaderHandler<ServiceTypeMapping> handler = new ExcelReaderHandler<ServiceTypeMapping>() {
            @Override
            public void onStart() {
                serviceTypeMappingRepository.delete();
            }

            @Override
            public void onRead(List<ServiceTypeMapping> items) {
                try {
                    serviceTypeMappingRepository.saveAll(items);
                } catch (Exception e) {
                    e.printStackTrace();
                    new ResponseEntity<Integer>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            @Override
            public ServiceTypeMapping transform(ServiceTypeMapping item) {
                return MasterDataNormalizer.normalizeServiceTypeMapping.apply(item);
            }

            @Override
            public void onFinish(int rowCount) {
                result.setResult(new ResponseEntity<Integer>(HttpStatus.CREATED));
            }
        };
        return importExcel(request, classType, handler, result);
    }

    @PostMapping(value = "taxonomy/import")
    public DeferredResult<ResponseEntity<?>> importTaxonomy(MultipartHttpServletRequest request) {
        Class<Taxonomy> classType = Taxonomy.class;
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        ExcelReaderHandler<Taxonomy> handler = new ExcelReaderHandler<Taxonomy>() {
            @Override
            public void onStart() {
                taxonomyRepository.delete();
            }

            @Override
            public void onRead(List<Taxonomy> items) {
                try {
                    taxonomyRepository.saveAll(items);
                } catch (Exception e) {
                    e.printStackTrace();
                    new ResponseEntity<Integer>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            @Override
            public Taxonomy transform(Taxonomy item) {
                return MasterDataNormalizer.normalizeTaxonomy.apply(item);
            }

            @Override
            public void onFinish(int rowCount) {
                result.setResult(new ResponseEntity<Integer>(HttpStatus.CREATED));
            }
        };
        return importExcel(request, classType, handler, result);
    }

    @PostMapping(value = "rate-cards/import")
    public DeferredResult<ResponseEntity<?>> importRateCards(MultipartHttpServletRequest request) {
        Class<RateCard> classType = RateCard.class;
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        ExcelReaderHandler<RateCard> handler = new ExcelReaderHandler<RateCard>() {
            @Override
            public void onStart() {
                rateCardRepository.delete();
            }

            @Override
            public void onRead(List<RateCard> items) {
                try {
                    rateCardRepository.saveAll(items);
                } catch (Exception e) {
                    e.printStackTrace();
                    new ResponseEntity<Integer>(HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

            @Override
            public RateCard transform(RateCard item) {
                return MasterDataNormalizer.normalizeRateCards.apply(item);
            }

            @Override
            public void onFinish(int rowCount) {
                result.setResult(new ResponseEntity<Integer>(HttpStatus.CREATED));
            }
        };
        return importExcel(request, classType, handler, result);
    }

    private <T> DeferredResult<ResponseEntity<?>> importExcel(
        MultipartHttpServletRequest request,
        Class<T> classType,
        ExcelReaderHandler<T> handler,
        DeferredResult<ResponseEntity<?>> result
    ) {
        GenericExcelReader<T> excelParser = new GenericExcelReader<>(handler, classType);
        try {
            MultipartFile multipartFile = request.getFile("file");
            try (InputStream inputStream = multipartFile.getInputStream();) {
                excelParser.parse(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
