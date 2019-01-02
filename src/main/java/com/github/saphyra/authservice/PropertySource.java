package com.github.saphyra.authservice;

import java.util.List;

public interface PropertySource {
    String getRequestTypeHeader();

    String getRestTypeValue();

    String getUnauthorizedRedirection();

    String getAccessTokenCookie();

    String getUserIdCookie();

    List<String> getAllowedUris();
}
