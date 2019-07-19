package com.github.saphyra.integration.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.ErrorResponseResolver;
import com.github.saphyra.authservice.auth.UriConfiguration;

@TestConfiguration
@Import(MvcConfiguration.class)
public class AuthConfiguration {
    @MockBean
    private AuthDao authDao;

    @MockBean
    private ErrorResponseResolver errorResponseResolver;

    @MockBean
    private UriConfiguration uriConfiguration;
}
