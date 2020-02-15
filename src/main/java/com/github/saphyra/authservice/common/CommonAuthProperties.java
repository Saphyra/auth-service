package com.github.saphyra.authservice.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class CommonAuthProperties {
    @Value("${com.github.saphyra.authservice.auth.cookie.access-token-id:cookie-access-token-id}")
    private String accessTokenIdCookie;

    @Value("${com.github.saphyra.authservice.auth.cookie.user-id:cookie-user-id}")
    private String userIdCookie;

    @Value("${com.github.saphyra.authservice.auth.rest.request-type-header:}")
    private String requestTypeHeader;

    @Value("${com.github.saphyra.authservice.auth.rest.rest-type-value:}")
    private String restTypeValue;
}
