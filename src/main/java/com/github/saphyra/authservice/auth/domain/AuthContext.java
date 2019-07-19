package com.github.saphyra.authservice.auth.domain;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthContext {
    private final String requestUri;
    private final HttpMethod requestMethod;
    private final boolean isRest;
    private final Optional<String> accessTokenId;
    private final Optional<String> userId;
    private final AccessStatus accessStatus;
    private final HttpServletRequest request;
}
