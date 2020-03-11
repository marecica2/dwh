package org.bmsource.dwh.importer.masterdata;

import org.bmsource.dwh.common.masterdata.model.RateCard;
import org.bmsource.dwh.common.masterdata.model.ServiceTypeMapping;
import org.bmsource.dwh.common.masterdata.model.Taxonomy;
import org.bmsource.dwh.common.masterdata.model.ZipCodeLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;

@Configuration
public class MasterDataRepositoryConfiguration implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.exposeIdsFor(ZipCodeLocation.class);
        config.exposeIdsFor(Taxonomy.class);
        config.exposeIdsFor(ServiceTypeMapping.class);
        config.exposeIdsFor(RateCard.class);
        config.setBasePath("/");
    }
}
