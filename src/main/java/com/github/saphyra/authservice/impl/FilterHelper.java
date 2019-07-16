package com.github.saphyra.authservice.impl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.saphyra.authservice.ErrorResponseResolver;
import com.github.saphyra.authservice.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.domain.AuthContext;
import com.github.saphyra.authservice.domain.RestErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class FilterHelper {
    private final ErrorResponseResolver errorResponseResolver;
    private final ObjectMapper objectMapper;
    private final PropertyConfiguration propertyConfiguration;

    void handleAccessDenied(HttpServletRequest request, HttpServletResponse response, AuthContext authContext) throws IOException {
        if (propertyConfiguration.getRestTypeValue().equals(request.getHeader(propertyConfiguration.getRequestTypeHeader()))) {
            log.info("Sending error. Cause: Access denied. AccessStatus: {}", authContext.getAccessStatus());
            RestErrorResponse errorResponse = errorResponseResolver.getRestErrorResponse(authContext);
            response.sendError(errorResponse.getHttpStatus().value(), safeToJson(errorResponse.getResponseBody()));
        } else {
            String redirectionPath = errorResponseResolver.getRedirectionPath(authContext);
            log.info("Redirecting to {}", redirectionPath);
            response.sendRedirect(redirectionPath);
        }
    }

    //TODO eliminate after adding ObjectMapperWrapper to util lib
    private String safeToJson(Object responseBody) {
        try {
            return objectMapper.writeValueAsString(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
