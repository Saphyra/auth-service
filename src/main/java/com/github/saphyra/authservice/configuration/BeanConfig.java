package com.github.saphyra.authservice.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.github.saphyra.authservice.impl.AuthFilter;

@Configuration
@ComponentScan(basePackages = "com.github.saphyra.authservice")
public class BeanConfig {
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterFilterRegistrationBean(
        AuthFilter authFilter,
        PropertyConfiguration propertyConfiguration
    ) {
        FilterRegistrationBean<AuthFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(authFilter);
        filterRegistrationBean.setOrder(propertyConfiguration.getFilterOrder());
        return filterRegistrationBean;
    }
}
