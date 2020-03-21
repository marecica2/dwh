package org.bmsource.dwh.common.job;

import org.bmsource.dwh.common.appstate.AppStateService;
import org.bmsource.dwh.common.filemanager.FileManager;
import org.bmsource.dwh.common.filemanager.ResourceFileManager;
import org.bmsource.dwh.common.utils.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableImportJob
public class ImporterConfiguration {

    @Bean
    @Primary
    FileManager fileManager() {
        return new ResourceFileManager();
    }

    @Bean
    @Primary
    AppStateService appStateService() {
        return (tenant, project, stateType, state) -> {
        };
    }

    @Bean
    ImportJobConfiguration<JobServiceIT.RawFact, JobServiceIT.Fact> importJobConfiguration() {
        return ImportJobConfigurationBuilder.<JobServiceIT.RawFact, JobServiceIT.Fact>get()
            .withBaseEntity(new JobServiceIT.RawFact())
            .withMappedEntity(new JobServiceIT.Fact())
            .withMapper((ctx, item) -> {
                JobServiceIT.Fact fact = new JobServiceIT.Fact();
                fact.setBillableWeight(item.getBillableWeight());
                fact.setBusinessUnit(item.getBusinessUnit());
                fact.setCost(item.getCost());
                fact.setServiceType(StringUtils.normalize(item.getServiceType()));
                fact.setTransactionId(item.getTransactionId());
                return fact;
            })
            .onCleanUp(ctx -> {
                System.out.println("Performing cleanup");
                return 0;
            })
            .build();
    }
}
