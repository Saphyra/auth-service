package com.github.saphyra.authservice.common;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestHelper {
    private final CommonPropertyConfiguration commonPropertyConfiguration;

    public HttpMethod getMethod(HttpServletRequest request){
        return HttpMethod.resolve(request.getMethod());
    }

    public boolean isRestCall(HttpServletRequest request) {
        return commonPropertyConfiguration.getRestTypeValue().equals(request.getHeader(commonPropertyConfiguration.getRequestTypeHeader()));
    }
}
