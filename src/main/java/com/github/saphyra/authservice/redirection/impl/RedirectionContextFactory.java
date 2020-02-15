package com.github.saphyra.authservice.redirection.impl;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.github.saphyra.authservice.common.CommonAuthProperties;
import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.authservice.redirection.domain.RedirectionContext;
import com.github.saphyra.util.CookieUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class RedirectionContextFactory {
    private final CookieUtil cookieUtil;
    private final CommonAuthProperties commonAuthProperties;
    private final RequestHelper requestHelper;

    RedirectionContext create(HttpServletRequest request) {
        return RedirectionContext.builder()
            .requestUri(request.getRequestURI())
            .requestMethod(requestHelper.getMethod(request))
            .isRest(requestHelper.isRestCall(request))
            .accessTokenId(cookieUtil.getCookie(request, commonAuthProperties.getAccessTokenIdCookie()))
            .userId(cookieUtil.getCookie(request, commonAuthProperties.getUserIdCookie()))
            .request(request)
            .build();
    }
}
