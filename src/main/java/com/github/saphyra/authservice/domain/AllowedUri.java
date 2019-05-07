package com.github.saphyra.authservice.domain;

import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class AllowedUri {
    private final String uri;
    private final Set<HttpMethod> allowedMethods = new HashSet<>();

    public AllowedUri(String uri, HttpMethod allowedMethod) {
        this(uri, Collections.singletonList(allowedMethod));
    }

    public AllowedUri(String uri, Collection<HttpMethod> allowedMethods) {
        if (uri == null) {
            throw new IllegalArgumentException("uri must not be null.");
        }

        if (allowedMethods == null) {
            throw new IllegalArgumentException("allowedMethods must not be null.");
        }
        if (allowedMethods.isEmpty()) {
            throw new IllegalArgumentException("allowedMethods must not be empty.");
        }

        this.uri = uri;
        this.allowedMethods.addAll(allowedMethods);
    }
}