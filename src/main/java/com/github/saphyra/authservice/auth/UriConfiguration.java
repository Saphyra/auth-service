package com.github.saphyra.authservice.auth;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.saphyra.authservice.auth.domain.AllowedUri;
import com.github.saphyra.authservice.auth.domain.RoleSetting;


public interface UriConfiguration {

    /**
     * List of URIs and Http methods that do NOT require authorization and authentication.
     *
     * @return list of allowed URIs
     */
    default List<AllowedUri> getAllowedUris() {
        return Collections.emptyList();
    }

    /**
     * List of URIs and Http methods that do NOT extend the user's session when called.
     *
     * @return list of non-extending uris
     */
    default List<AllowedUri> getNonSessionExtendingUris() {
        return Collections.emptyList();
    }

    /**
     * If an URI requires special role(s) to access, add to this map with the protected http methods..
     * <p>
     * Uri: Ant pattern of the URI.
     * protectedMethods: role check is executed when the request has any of these methods.
     * roles: Set of Roles. The user has to have at least one of these roles to access the URI.
     * <p>
     * Example: Endpoints start with "admin/" with method POST can only be access with role "ADMIN".
     * uri: admin/**
     * protectedMethods: [HttpMethod.POST]
     * roles: [ADMIN]
     * <p>
     * Example: Endpoints start with "user/" with all methods can be access with role "USER" and "ADMIN", but "VISITOR" cannot.
     * uri: user/**
     * protectedMethods [HttpMethod.values()]
     * roles: [ADMIN, USER]
     *
     * @return Map of URI patterns and Necessary Role(s) to access them.
     */
    default Set<RoleSetting> getRoleSettings() {
        return new HashSet<>();
    }
}
