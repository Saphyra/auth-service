package com.github.saphyra.authservice.integration.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.AntPathMatcher;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.EnableAuthService;
import com.github.saphyra.authservice.auth.ErrorResponseResolver;
import com.github.saphyra.authservice.auth.UriConfiguration;
import com.github.saphyra.exceptionhandling.EnableExceptionHandler;
import com.github.saphyra.util.IdGenerator;
import com.github.saphyra.util.OffsetDateTimeProvider;

@TestConfiguration
@ComponentScan(basePackages = {
    "com.github.saphyra.util"
})
@TestPropertySource(locations = "classpath:application.properties")
@EnableExceptionHandler
@EnableAuthService
public class MvcConfiguration {
    @MockBean
    private AuthDao authDao;

    @MockBean
    private ErrorResponseResolver errorResponseResolver;

    @MockBean
    private UriConfiguration uriConfiguration;

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
