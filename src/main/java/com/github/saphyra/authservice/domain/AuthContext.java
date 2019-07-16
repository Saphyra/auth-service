package com.github.saphyra.authservice.domain;

import java.util.Optional;

import org.springframework.http.HttpMethod;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class AuthContext {
    private final String requestUri;
    private final HttpMethod requestMethod;
    private final Optional<String> accessTokenId;
    private final Optional<String> userId;
    private final AccessStatus accessStatus;
}
