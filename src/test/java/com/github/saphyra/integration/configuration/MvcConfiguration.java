package com.github.saphyra.integration.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.AntPathMatcher;

import com.github.saphyra.authservice.redirection.EnableRedirection;
import com.github.saphyra.exceptionhandling.EnableExceptionHandler;
import com.github.saphyra.util.IdGenerator;
import com.github.saphyra.util.OffsetDateTimeProvider;

@TestConfiguration
@ComponentScan(basePackages = {
    "com.github.saphyra.util",
    "com.github.saphyra.integration.component"
})
@TestPropertySource(locations = "classpath:application.properties")
@EnableExceptionHandler
@EnableRedirection
public class MvcConfiguration {
    @Bean
    public AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }

    @Bean
    public IdGenerator idGenerator() {
        return new IdGenerator();
    }

    @Bean
    public OffsetDateTimeProvider offsetDateTimeProvider() {
        return new OffsetDateTimeProvider();
    }
}