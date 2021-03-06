package com.github.saphyra.authservice.auth.impl;


import com.github.saphyra.authservice.auth.AuthService;
import com.github.saphyra.authservice.auth.UriConfiguration;
import com.github.saphyra.authservice.auth.configuration.AuthProperties;
import com.github.saphyra.authservice.auth.domain.AccessStatus;
import com.github.saphyra.authservice.auth.domain.AllowedUri;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.common.CommonAuthProperties;
import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.PostConstruct;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthFilter extends OncePerRequestFilter {
    private final Set<AllowedUri> allowedUris = new HashSet<>();
    private final AuthContextFactory authContextFactory;
    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final FilterHelper filterHelper;
    private final AntPathMatcher pathMatcher;
    private final AuthProperties authProperties;
    private final CommonAuthProperties commonAuthProperties;
    private final RequestHelper requestHelper;
    private final UriConfiguration uriConfiguration;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("Filtering...");
        String path = request.getRequestURI();
        HttpMethod method = requestHelper.getMethod(request);
        log.debug("Request arrived: {} - {}", path, method);
        if (isAllowedPath(path, method)) {
            log.debug("Allowed path: {}", path);
            filterChain.doFilter(request, response);
        } else {
            log.debug("Protected path: {}", path);
            Optional<String> accessTokenId = cookieUtil.getCookie(request, commonAuthProperties.getAccessTokenIdCookie());
            Optional<String> userId = cookieUtil.getCookie(request, commonAuthProperties.getUserIdCookie());
            AccessStatus accessStatus = getAccessStatus(path, method, accessTokenId, userId);
            if (accessStatus == AccessStatus.GRANTED) {
                log.debug("Access granted.: {}", path);
                filterChain.doFilter(request, response);
            } else {
                AuthContext authContext = authContextFactory.create(request, accessStatus);
                filterHelper.handleAccessDenied(request, response, authContext);
            }
        }
    }

    private boolean isAllowedPath(String path, HttpMethod method) {
        return allowedUris.stream()
            .filter(allowedUri -> pathMatcher.match(allowedUri.getUri(), path))
            .anyMatch(allowedUri -> allowedUri.getAllowedMethods().contains(method));
    }

    private AccessStatus getAccessStatus(String requestUri, HttpMethod requestMethod, Optional<String> accessTokenId, Optional<String> userIdValue) {
        log.debug("Authenticating...");
        if (!accessTokenId.isPresent() || !userIdValue.isPresent()) {
            log.warn("Cookies not found.");
            return AccessStatus.COOKIE_NOT_FOUND;
        }

        return authService.canAccess(
            requestUri,
            requestMethod,
            userIdValue.get(),
            accessTokenId.get()
        );
    }

    @PostConstruct
    void mapAllowedUris() {
        allowedUris.addAll(authProperties.getDefaultAllowedUris());
        allowedUris.addAll(uriConfiguration.getAllowedUris());
        log.debug("AllowedUris: {}", allowedUris);
    }
}
