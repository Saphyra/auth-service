package com.github.saphyra.authservice.filter;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import static com.github.saphyra.authservice.controller.AuthController.LOGIN_MAPPING;


@Slf4j
@RequiredArgsConstructor
@Component
public class AuthFilter extends OncePerRequestFilter {
    private static final List<String> DEFAULT_ALLOWED_URIS = Arrays.asList(
        "/" + LOGIN_MAPPING
    );

    private final AntPathMatcher pathMatcher;
    private final AuthService authService;
    private final FilterHelper filterHelper;
    private final PropertySource propertySource;
    private final Set<String> allowedUris = new HashSet<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("AuthFilter");
        String path = request.getRequestURI();
        log.debug("Request arrived: {}", path);
        if (isAllowedPath(path)) {
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

    private boolean isAllowedPath(String path) {
        return allowedUris.stream()
            .anyMatch(allowedPath -> pathMatcher.match(allowedPath, path));
    }

    private AccessStatus getAccessStatus(HttpServletRequest request) {
        log.debug("Authenticating...");
        Optional<String> accessTokenId = CookieUtil.getCookie(request, propertySource.getAccessTokenIdCookie());
        Optional<String> userIdValue = CookieUtil.getCookie(request, propertySource.getUserIdCookie());

        if (!accessTokenId.isPresent() || !userIdValue.isPresent()) {
            log.warn("Cookies not found.");
            return AccessStatus.UNAUTHORIZED;
        }

        return authService.canAccess(request.getRequestURI(), userIdValue.get(), accessTokenId.get());
    }

    @PostConstruct
    public void mapAllowedUris() {
        allowedUris.addAll(DEFAULT_ALLOWED_URIS);
        allowedUris.addAll(propertySource.getAllowedUris());
    }
}
