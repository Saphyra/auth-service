package com.github.saphyra.authservice.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import com.github.saphyra.authservice.impl.AuthFilter;
import com.github.saphyra.util.IdGenerator;
import com.github.saphyra.util.OffsetDateTimeProvider;

@Configuration
@ComponentScan(basePackages = "com.github.saphyra.authservice")
public class BeanConfig {
    @Bean
    public IdGenerator idGenerator() {
        return new IdGenerator();
    }

    @Bean
    public OffsetDateTimeProvider offsetDateTimeProvider() {
        return new OffsetDateTimeProvider();
    }

    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

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
