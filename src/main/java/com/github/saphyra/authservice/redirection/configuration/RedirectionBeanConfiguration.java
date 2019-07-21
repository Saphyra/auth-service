package com.github.saphyra.authservice.redirection.configuration;

import com.github.saphyra.authservice.redirection.impl.RedirectionFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "com.github.saphyra.authservice.redirection",
    "com.github.saphyra.authservice.common"
})
@Slf4j
public class RedirectionBeanConfiguration {

    @Bean
    public FilterRegistrationBean<RedirectionFilter> redirectionFilterFilterRegistrationBean(
        RedirectionFilter redirectionFilter,
        RedirectionPropertyConfiguration redirectionPropertyConfiguration
    ) {
        log.info("RedirectionFilter order: {}", redirectionPropertyConfiguration.getFilterOrder());
        FilterRegistrationBean<RedirectionFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(redirectionFilter);
        filterRegistrationBean.setOrder(redirectionPropertyConfiguration.getFilterOrder());
        return filterRegistrationBean;
    }
}
