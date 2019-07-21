package com.github.saphyra.authservice.redirection.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@Data
public class ProtectedUri {
    private final List<String> requestUris;
    private final Set<HttpMethod> protectedMethods;

    public ProtectedUri(String requestUri, HttpMethod protectedMethod) {
        this(Arrays.asList(requestUri), new HashSet<>(Arrays.asList(protectedMethod)));
    }

    public ProtectedUri(List<String> requestUris, HttpMethod protectedMethod) {
        this(requestUris, new HashSet<>(Arrays.asList(protectedMethod)));
    }

    public ProtectedUri(String requestUri, Set<HttpMethod> protectedMethod) {
        this(Arrays.asList(requestUri), protectedMethod);
    }
}
