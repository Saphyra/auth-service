package com.github.saphyra.authservice.auth;

import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.auth.domain.RestErrorResponse;

public interface ErrorResponseResolver {
    RestErrorResponse getRestErrorResponse(AuthContext authContext);

    String getRedirectionPath(AuthContext authContext);
}
