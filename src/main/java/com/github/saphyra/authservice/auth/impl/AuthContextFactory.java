package com.github.saphyra.authservice.auth.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.github.saphyra.authservice.auth.domain.AccessStatus;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.common.CommonAuthProperties;
import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.util.CookieUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class AuthContextFactory {
    private final CookieUtil cookieUtil;
    private final CommonAuthProperties commonAuthProperties;
    private final RequestHelper requestHelper;

    AuthContext create(HttpServletRequest request, AccessStatus accessStatus) {
        return AuthContext.builder()
            .requestUri(request.getRequestURI())
            .requestMethod(requestHelper.getMethod(request))
            .isRest(requestHelper.isRestCall(request))
            .accessTokenId(cookieUtil.getCookie(request, commonAuthProperties.getAccessTokenIdCookie()))
            .userId(cookieUtil.getCookie(request, commonAuthProperties.getUserIdCookie()))
            .accessStatus(accessStatus)
            .request(request)
            .build();
    }
}
