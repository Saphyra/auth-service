package com.github.saphyra.authservice.redirection.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Data
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString(exclude = "request")
public class RedirectionContext {
    private final String requestUri;
    private final HttpMethod requestMethod;
    private final boolean isRest;
    private final Optional<String> accessTokenId;
    private final Optional<String> userId;
    private final HttpServletRequest request;
}
