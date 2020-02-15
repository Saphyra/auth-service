package com.github.saphyra.authservice.common;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class AuthBeanConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }
}
