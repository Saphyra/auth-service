package com.github.saphyra.authservice.impl;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AllowedUri;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.github.saphyra.authservice.impl.AuthController.LOGIN_MAPPING;
import static com.github.saphyra.authservice.impl.AuthController.LOGOUT_MAPPING;


@Slf4j
@RequiredArgsConstructor
@Component
public class AuthFilter extends OncePerRequestFilter {
    private static final List<AllowedUri> DEFAULT_ALLOWED_URIS = Arrays.asList(
        new AllowedUri("/" + LOGIN_MAPPING, HttpMethod.POST),
        new AllowedUri("/" + LOGOUT_MAPPING, HttpMethod.POST)
    );

    private final AntPathMatcher pathMatcher;
    private final AuthService authService;
    private final FilterHelper filterHelper;
    private final PropertySource propertySource;
    private final Set<AllowedUri> allowedUris = new HashSet<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("AuthFilter");
        String path = request.getRequestURI();
        HttpMethod method = HttpMethod.resolve(request.getMethod());
        log.debug("Request arrived: {}", path);
        if (isAllowedPath(path, method)) {
            log.debug("Allowed path: {}", path);
            filterChain.doFilter(request, response);
        } else {
            AccessStatus accessStatus = getAccessStatus(request);
            if (accessStatus == AccessStatus.GRANTED) {
                log.debug("Access granted.: {}", path);
                filterChain.doFilter(request, response);
            } else {
                filterHelper.handleUnauthorized(request, response, accessStatus);
            }
        }
    }

    private boolean isAllowedPath(String path, HttpMethod method) {
        return allowedUris.stream()
            .filter(allowedUri -> pathMatcher.match(allowedUri.getUri(), path))
            .anyMatch(allowedUri -> allowedUri.getAllowedMethods().contains(method));
    }

    private AccessStatus getAccessStatus(HttpServletRequest request) {
        log.debug("Authenticating...");
        Optional<String> accessTokenId = CookieUtil.getCookie(request, propertySource.getAccessTokenIdCookie());
        Optional<String> userIdValue = CookieUtil.getCookie(request, propertySource.getUserIdCookie());

        if (!accessTokenId.isPresent() || !userIdValue.isPresent()) {
            log.warn("Cookies not found.");
            return AccessStatus.UNAUTHORIZED;
        }

        return authService.canAccess(
            request.getRequestURI(),
            HttpMethod.resolve(request.getMethod()),
            userIdValue.get(),
            accessTokenId.get()
        );
    }

    @PostConstruct
    void mapAllowedUris() {
        allowedUris.addAll(DEFAULT_ALLOWED_URIS);
        allowedUris.addAll(propertySource.getAllowedUris());
    }
}