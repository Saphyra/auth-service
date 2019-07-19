package com.github.saphyra.authservice.redirection.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.authservice.redirection.RedirectionFilterSettings;
import com.github.saphyra.authservice.redirection.domain.RedirectionContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedirectionFilter extends OncePerRequestFilter {
    private final AntPathMatcher antPathMatcher;
    private final List<RedirectionFilterSettings> redirectionFilterSettings;
    private final RedirectionContextFactory redirectionContextFactory;
    private final RequestHelper requestHelper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional<RedirectionFilterSettings> settingsOptional = getMatchingFilter(request);
        if (settingsOptional.isPresent()) {
            RedirectionFilterSettings redirectionFilterSettings = settingsOptional.get();
            RedirectionContext redirectionContext = redirectionContextFactory.create(request);
            if (redirectionFilterSettings.shouldRedirect(redirectionContext)) {
                response.sendRedirect(redirectionFilterSettings.getRedirectionPath(redirectionContext));
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Optional<RedirectionFilterSettings> getMatchingFilter(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        HttpMethod requestMethod = requestHelper.getMethod(request);
        return redirectionFilterSettings.stream()
            .filter(redirectionSetting -> antPathMatcher.match(redirectionSetting.getProtectedUri().getRequestUri(), requestUri))
            .filter(redirectionSetting -> redirectionSetting.getProtectedUri().getProtectedMethods().contains(requestMethod))
            .findAny();
    }
}
