package com.github.saphyra.authservice.impl;

import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
class FilterHelper {
    private static final String DEFAULT_REDIRECTION = "/";

    private final PropertySource propertySource;

    void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, AccessStatus accessStatus) throws IOException {
        if (propertySource.getRestTypeValue().equals(request.getHeader(propertySource.getRequestTypeHeader()))) {
            log.info("Sending error. Cause: Access denied. AccessStatus: {}", accessStatus);
            response.sendError(accessStatus.getResponseStatus(), "Access denied: " + accessStatus.name());
        } else {
            String redirection = DEFAULT_REDIRECTION;
            switch (accessStatus) {
                case FORBIDDEN:
                    redirection = propertySource.getForbiddenRedirection();
                    break;
                case UNAUTHORIZED:
                    redirection = propertySource.getUnauthorizedRedirection();
                    break;
            }

            log.info("Redirect to {}. Cause: Access denied. AccessStatus: {}", redirection, accessStatus);
            response.sendRedirect(redirection);
        }
    }
}
