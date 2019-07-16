package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.AuthContext;
import com.github.saphyra.authservice.domain.RestErrorResponse;

public interface ErrorResponseResolver {
    RestErrorResponse getRestErrorResponse(AuthContext authContext);

    String getRedirectionPath(AuthContext authContext);
}
