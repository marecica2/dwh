package org.bmsource.dwh.importer.masterdata;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

@Configuration
public class MasterDataConfiguration {

    @Bean
    MultipartResolver multipartResolver() {
        return new CommonsMultipartResolver();
    }
}
