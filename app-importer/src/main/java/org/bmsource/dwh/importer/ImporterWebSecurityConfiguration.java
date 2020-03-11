//package org.bmsource.dwh;
//
//import org.bmsource.dwh.common.security.client.BasicWebSecurityConfiguration;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//
//public class ImporterWebSecurityConfiguration extends BasicWebSecurityConfiguration {
//
////    @Override
////    public void configure(HttpSecurity http) throws Exception {
////        System.out.println("BBBB ImporterWebSecurityConfiguration");
////        //super.configure(http);
////        http
////            .authorizeRequests()
////            .antMatchers("/status**", "/actuator/health", "api/importer/actuator/health")
////            .permitAll();
////    }
//}
