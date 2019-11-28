package org.bmsource.dwh.masterdata.web;

import org.bmsource.dwh.common.BaseFact;
import org.bmsource.dwh.masterdata.*;
import org.bmsource.dwh.masterdata.model.RateCard;
import org.bmsource.dwh.masterdata.model.ServiceTypeMapping;
import org.bmsource.dwh.masterdata.model.Taxonomy;
import org.bmsource.dwh.masterdata.model.ZipCodeLocation;
import org.bmsource.dwh.masterdata.repository.RateCardRepository;
import org.bmsource.dwh.masterdata.repository.ServiceTypeMappingRepository;
import org.bmsource.dwh.masterdata.repository.TaxonomyRepository;
import org.bmsource.dwh.masterdata.repository.ZipCodeLocationRepository;
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
import java.util.function.Consumer;

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

    @PostMapping(value = "/zip-code-locations/import")
    public DeferredResult<ResponseEntity<?>> importZipCodes(MultipartHttpServletRequest request) {
        Class<ZipCodeLocation> classType = ZipCodeLocation.class;
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        ExcelReaderHandler<ZipCodeLocation> handler = new ExcelReaderHandler<ZipCodeLocation>() {
            @Override
            public void onStart() {
                zipCodeRepository.deleteAll();
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
            public void onFinish(int rowCount) {
                result.setResult(new ResponseEntity<Integer>(HttpStatus.CREATED));
            }
        };
        return importModel(request, classType, handler, result);
    }

    @PostMapping(value = "/service-types/import")
    public DeferredResult<ResponseEntity<?>> importServiceTypes(MultipartHttpServletRequest request) {
        Class<ServiceTypeMapping> classType = ServiceTypeMapping.class;
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        ExcelReaderHandler<ServiceTypeMapping> handler = new ExcelReaderHandler<ServiceTypeMapping>() {
            @Override
            public void onStart() {
                serviceTypeMappingRepository.deleteAll();
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
            public void onFinish(int rowCount) {
                result.setResult(new ResponseEntity<Integer>(HttpStatus.CREATED));
            }
        };
        return importModel(request, classType, handler, result);
    }

    @PostMapping(value = "/taxonomy/import")
    public DeferredResult<ResponseEntity<?>> importTaxonomy(MultipartHttpServletRequest request) {
        Class<Taxonomy> classType = Taxonomy.class;
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();
        ExcelReaderHandler<Taxonomy> handler = new ExcelReaderHandler<Taxonomy>() {
            @Override
            public void onStart() {
                taxonomyRepository.deleteAll();
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
            public void onFinish(int rowCount) {
                result.setResult(new ResponseEntity<Integer>(HttpStatus.CREATED));
            }
        };
        return importModel(request, classType, handler, result);
    }

    @PostMapping(value = "/rate-cards/import")
    public DeferredResult<ResponseEntity<?>> importRateCards(MultipartHttpServletRequest request) {
        Class<RateCard> classType = RateCard.class;
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>();

        ExcelReaderHandler<RateCard> handler = new ExcelReaderHandler<RateCard>() {
            @Override
            public void onStart() {
                rateCardRepository.deleteAll();
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
            public void onFinish(int rowCount) {
                result.setResult(new ResponseEntity<Integer>(HttpStatus.CREATED));
            }
        };
        return importModel(request, classType, handler, result);
    }

    private <T extends BaseFact> DeferredResult<ResponseEntity<?>> importModel(MultipartHttpServletRequest request,
                                                                               Class<T> classType,
                                                                               ExcelReaderHandler<T> handler,
                                                                               DeferredResult<ResponseEntity<?>> result
    ) {
        GenericExcelReader excelParser = new GenericExcelReader<T>(handler, classType);
        try {
            MultipartFile multipartFile = request.getFile("file");
            String name = multipartFile.getOriginalFilename();
            try (InputStream inputStream = multipartFile.getInputStream();) {
                excelParser.parse(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
