package com.github.saphyra.integration.configuration;

import static com.github.saphyra.integration.component.TestController.ALLOWED_URI_MAPPING;
import static com.github.saphyra.integration.component.TestController.PROTECTED_URI_MAPPING;
import static org.mockito.BDDMockito.given;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.EnableAuthService;
import com.github.saphyra.authservice.auth.ErrorResponseResolver;
import com.github.saphyra.authservice.auth.UriConfiguration;
import com.github.saphyra.authservice.auth.domain.AllowedUri;

@TestConfiguration
@Import(MvcConfiguration.class)
@EnableAuthService
public class AuthConfiguration {
    @MockBean
    private AuthDao authDao;

    @MockBean
    private ErrorResponseResolver errorResponseResolver;

    @Bean
    public UriConfiguration uriConfiguration() {
        UriConfiguration uriConfiguration = Mockito.mock(UriConfiguration.class);
        given(uriConfiguration.getAllowedUris()).willReturn(getAllowedUris());
        return uriConfiguration;
    }

    private List<AllowedUri> getAllowedUris() {
        return Arrays.asList(
            new AllowedUri(ALLOWED_URI_MAPPING, new HashSet<>(Arrays.asList(HttpMethod.values()))),
            new AllowedUri(PROTECTED_URI_MAPPING, new HashSet<>(Arrays.asList(HttpMethod.PUT)))
        );
    }
}
