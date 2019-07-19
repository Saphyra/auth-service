package com.github.saphyra.integration.component;

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class ResponseValidator {
    public void verifyRedirection(MockHttpServletResponse response, String redirectionPath) {
        assertThat(response.getStatus()).isEqualTo(302);
        assertThat(response.getHeader("Location")).isEqualTo(redirectionPath);
    }
}
