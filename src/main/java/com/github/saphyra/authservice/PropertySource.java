package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PropertySource {
    String getRequestTypeHeader();

    String getRestTypeValue();

    String getUnauthorizedRedirection();

    String getAccessTokenCookie();

    String getUserIdCookie();

    List<String> getAllowedUris();

    Map<String, Set<Role>> getRoleSettings();

    boolean isMultipleLoginAllowed();

    long getTokenExpirationMinutes();
}
