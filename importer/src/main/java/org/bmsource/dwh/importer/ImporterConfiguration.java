package org.bmsource.dwh.importer;

import org.bmsource.dwh.common.job.EnableImportJob;
import org.bmsource.dwh.common.job.ImportJobConfiguration;
import org.bmsource.dwh.common.job.ImportJobConfigurationBuilder;
import org.bmsource.dwh.common.utils.StringUtils;
import org.bmsource.dwh.masterdata.MasterDataService;
import org.bmsource.dwh.model.Fact;
import org.bmsource.dwh.model.RawFact;
import org.bmsource.dwh.repository.FactRawRepository;
import org.bmsource.dwh.repository.FactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;

@EnableImportJob
@Configuration
@EntityScan
public class ImporterConfiguration {

    private static Logger logger = LoggerFactory.getLogger(ImportJobConfiguration.class);

    @Autowired
    MasterDataService masterDataService;

    @Autowired
    JdbcTemplate template;

    @Autowired
    FactRepository factRepository;

    @Autowired
    FactRawRepository rawFactRepository;

    @Bean
    ImportJobConfiguration<RawFact, Fact> importJobConfiguration() {
        return ImportJobConfigurationBuilder.<RawFact, Fact>get()
            .withBaseEntity(new RawFact())
            .withMappedEntity(new Fact())
            .withMapper(item -> {
                Fact fact = new Fact();
                fact.setTransactionId(item.getTransactionId());
                fact.setBusinessUnit(item.getBusinessUnit());
                fact.setSupplierName(StringUtils.normalize(item.getSupplierName()));
                fact.setServiceType(StringUtils.normalize(item.getServiceType()));
                fact.setStandardServiceType(masterDataService.getStandardServiceType(fact.getServiceType()));

                fact.setOriginCity(item.getOriginCity());
                fact.setOriginState(item.getOriginState());
                fact.setOriginZip(item.getOriginZip());
                fact.setOriginCountry(item.getOriginCountry());

                fact.setDestinationCity(item.getDestinationCity());
                fact.setDestinationState(item.getDestinationState());
                fact.setDestinationCountry(item.getDestinationCountry());
                fact.setDestinationZip(item.getDestinationZip());

                fact.setZone(item.getZone());
                fact.setDeliveryDate(item.getDeliveryDate());
                fact.setShipmentDate(item.getShipmentDate());
                fact.setCost(item.getCost());
                fact.setBillableWeight(item.getBillableWeight());
                fact.setActualWeight(item.getActualWeight());
                fact.setLength(item.getLength());
                fact.setWidth(item.getWidth());
                fact.setHeight(item.getHeight());

                fact.setAccessorialService1(item.getAccessorialService1());
                fact.setAccessorialCharge1(item.getAccessorialCharge1());
                fact.setAccessorialService2(item.getAccessorialService2());
                fact.setAccessorialCharge2(item.getAccessorialCharge2());
                return fact;
            })
            .onCleanUp(ctx -> {
                logger.info("Cleaning previous imported state");
                factRepository.deleteAll();
                rawFactRepository.deleteAll();
                return 1;
            })
            .build();
    }
}
