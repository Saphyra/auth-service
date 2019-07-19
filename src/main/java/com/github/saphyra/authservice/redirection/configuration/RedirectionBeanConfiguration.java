package com.github.saphyra.authservice.redirection.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.github.saphyra.authservice.redirection.impl.RedirectionFilter;

@Configuration
@ComponentScan(basePackages = {
    "com.github.saphyra.authservice.redirection",
    "com.github.saphyra.authservice.common"
})
public class RedirectionBeanConfiguration {

    @Bean
    public FilterRegistrationBean<RedirectionFilter> authFilterFilterRegistrationBean(
        RedirectionFilter redirectionFilter,
        RedirectionPropertyConfiguration redirectionPropertyConfiguration
    ) {
        FilterRegistrationBean<RedirectionFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(redirectionFilter);
        filterRegistrationBean.setOrder(redirectionPropertyConfiguration.getFilterOrder());
        return filterRegistrationBean;
    }
}
