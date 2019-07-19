package com.github.saphyra.authservice.auth.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.github.saphyra.authservice.auth.impl.AuthFilter;

@Configuration
@ComponentScan(basePackages = {
    "com.github.saphyra.authservice.auth",
    "com.github.saphyra.authservice.common"
})
public class AuthServiceBeanConfiguration {
    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterFilterRegistrationBean(
        AuthFilter authFilter,
        AuthPropertyConfiguration authPropertyConfiguration
    ) {
        FilterRegistrationBean<AuthFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(authFilter);
        filterRegistrationBean.setOrder(authPropertyConfiguration.getFilterOrder());
        return filterRegistrationBean;
    }
}
