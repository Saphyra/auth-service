package com.github.saphyra.authservice.common;

import com.github.saphyra.authservice.auth.ErrorResponseResolver;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.auth.domain.RestErrorResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultErrorResponseResolver implements ErrorResponseResolver {
    private final CommonAuthProperties commonAuthProperties;
    @Override
    public RestErrorResponse getRestErrorResponse(AuthContext authContext) {
        return new RestErrorResponse(
            authContext.getAccessStatus().getResponseStatus(),
            null
        );
    }

    @Override
    public String getRedirectionPath(AuthContext authContext) {
        return commonAuthProperties.getDefaultErrorRedirection();
    }
}
