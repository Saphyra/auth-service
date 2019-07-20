package com.github.saphyra.authservice.redirection.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@Data
public class ProtectedUri {
    private final String requestUri;
    private final Set<HttpMethod> protectedMethods;

    public ProtectedUri(String requestUri, HttpMethod protectedMethod){
        this(requestUri, new HashSet<>(Arrays.asList(protectedMethod)));
    }
}
