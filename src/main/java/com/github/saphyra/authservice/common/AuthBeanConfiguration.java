package com.github.saphyra.authservice.common;

import com.github.saphyra.authservice.auth.ErrorResponseResolver;
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

    @Bean
    @ConditionalOnMissingBean(ErrorResponseResolver.class)
    public ErrorResponseResolver errorResponseResolver(CommonAuthProperties commonAuthProperties) {
        return new DefaultErrorResponseResolver(commonAuthProperties);
    }
}
