package com.github.saphyra.authservice.auth.impl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;

import com.github.saphyra.authservice.auth.ErrorResponseResolver;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.auth.domain.RestErrorResponse;
import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.util.ObjectMapperWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class FilterHelper {
    private final ErrorResponseResolver errorResponseResolver;
    private final ObjectMapperWrapper objectMapperWrapper;
    private final RequestHelper requestHelper;

    void handleAccessDenied(HttpServletRequest request, HttpServletResponse response, AuthContext authContext) throws IOException {
        if (requestHelper.isRestCall(request)) {
            log.info("Sending error. Cause: Access denied. AccessStatus: {}", authContext.getAccessStatus());
            RestErrorResponse errorResponse = errorResponseResolver.getRestErrorResponse(authContext);
            response.setStatus(errorResponse.getHttpStatus().value());
            PrintWriter writer = response.getWriter();
            writer.write(objectMapperWrapper.writeValueAsString(errorResponse.getResponseBody()));
            writer.flush();
            writer.close();
        } else {
            String redirectionPath = errorResponseResolver.getRedirectionPath(authContext);
            log.info("Redirecting to {}", redirectionPath);
            response.sendRedirect(redirectionPath);
        }
    }
}
