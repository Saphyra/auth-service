package com.github.saphyra.authservice.redirection.impl;

import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.authservice.redirection.RedirectionFilterSettings;
import com.github.saphyra.authservice.redirection.domain.RedirectionContext;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Builder
public class RedirectionFilter extends OncePerRequestFilter {
    private final AntPathMatcher antPathMatcher;
    private final List<RedirectionFilterSettings> redirectionFilterSettings;
    private final RedirectionContextFactory redirectionContextFactory;
    private final RequestHelper requestHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("Filtering...");
        List<RedirectionFilterSettings> matchingSettings = getMatchingFilter(request);
        log.debug("matchingSettings: {}", matchingSettings);
        for (RedirectionFilterSettings redirectionFilterSettings : matchingSettings) {
            RedirectionContext redirectionContext = redirectionContextFactory.create(request);
            log.debug("redirectionContext: {}", redirectionContext);
            if (redirectionFilterSettings.shouldRedirect(redirectionContext)) {
                String redirectionPath = redirectionFilterSettings.getRedirectionPath(redirectionContext);
                log.debug("Redirecting to {}", redirectionPath);
                response.sendRedirect(redirectionPath);
                return;
            }
        }

        log.debug("Redirection is not necessary.");
        filterChain.doFilter(request, response);
    }

    private List<RedirectionFilterSettings> getMatchingFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        HttpMethod requestMethod = requestHelper.getMethod(request);
        log.debug("requestUri: {}, requestMethod: {}", requestUri, requestMethod);
        return redirectionFilterSettings.stream()
            .sorted(Comparator.comparing(RedirectionFilterSettings::getFilterOrder))
            .filter(redirectionSetting -> antPathMatcher.match(redirectionSetting.getProtectedUri().getRequestUri(), requestUri))
            .filter(redirectionSetting -> redirectionSetting.getProtectedUri().getProtectedMethods().contains(requestMethod))
            .collect(Collectors.toList());
    }
}
