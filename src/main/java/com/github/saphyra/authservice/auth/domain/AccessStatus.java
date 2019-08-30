package com.github.saphyra.authservice.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AccessStatus {
    GRANTED(HttpStatus.OK),
    LOGIN_FAILED(HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    ACCESS_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED),
    COOKIE_NOT_FOUND(HttpStatus.UNAUTHORIZED),
    INVALID_USER_ID(HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN);

    @Getter
    private final HttpStatus responseStatus;
}
