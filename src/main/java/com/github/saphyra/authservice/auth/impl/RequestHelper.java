package com.github.saphyra.authservice.auth.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.github.saphyra.authservice.auth.configuration.PropertyConfiguration;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class RequestHelper {
    private final PropertyConfiguration propertyConfiguration;

    HttpMethod getMethod(HttpServletRequest request){
        return HttpMethod.resolve(request.getMethod());
    }

    boolean isRestCall(HttpServletRequest request) {
        return propertyConfiguration.getRestTypeValue().equals(request.getHeader(propertyConfiguration.getRequestTypeHeader()));
    }
}
