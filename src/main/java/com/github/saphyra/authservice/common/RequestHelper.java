package com.github.saphyra.authservice.common;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestHelper {
    private final CommonAuthProperties commonAuthProperties;

    public HttpMethod getMethod(HttpServletRequest request){
        return HttpMethod.resolve(request.getMethod());
    }

    public boolean isRestCall(HttpServletRequest request) {
        return commonAuthProperties.getRestTypeValue().equals(request.getHeader(commonAuthProperties.getRequestTypeHeader()));
    }
}
