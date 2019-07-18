package com.github.saphyra.authservice.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.github.saphyra.authservice.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AuthContext;
import com.github.saphyra.util.CookieUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class AuthContextFactory {
    private final CookieUtil cookieUtil;
    private final PropertyConfiguration propertyConfiguration;
    private final RequestHelper requestHelper;

    AuthContext create(HttpServletRequest request, AccessStatus accessStatus) {
        return AuthContext.builder()
            .requestUri(request.getRequestURI())
            .requestMethod(requestHelper.getMethod(request))
            .isRest(requestHelper.isRestCall(request))
            .accessTokenId(cookieUtil.getCookie(request, propertyConfiguration.getAccessTokenIdCookie()))
            .userId(cookieUtil.getCookie(request, propertyConfiguration.getUserIdCookie()))
            .accessStatus(accessStatus)
            .request(request)
            .build();
    }
}
