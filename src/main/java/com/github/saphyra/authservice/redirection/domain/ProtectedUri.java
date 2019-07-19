package com.github.saphyra.authservice.redirection.domain;

import java.util.Set;

import org.springframework.http.HttpMethod;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ProtectedUri {
    private final String requestUri;
    private final Set<HttpMethod> protectedMethods;
}
