package com.github.saphyra.authservice.filter;

import com.github.saphyra.authservice.PropertySource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FilterHelper {
    private final PropertySource propertySource;

    public void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, String redirection) throws IOException {
        if (propertySource.getRestTypeValue().equals(request.getHeader(propertySource.getRequestTypeHeader()))) {
            log.info("Sending error. Cause: Unauthorized access.");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed.");
        } else {
            log.info("Redirect to {}. Cause: Unauthorized access.", redirection);
            response.sendRedirect(redirection);
        }
    }
}
