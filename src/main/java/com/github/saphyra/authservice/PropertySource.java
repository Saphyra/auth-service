package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.Role;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PropertySource {
    /**
     * RequestType is used to determinate the request purpose. It is a REST query, or GET for a static resource, etc.
     *
     * @return header name of RequestType determination.
     */
    String getRequestTypeHeader();

    /**
     * The value of the header even the request is REST.
     *
     * @return RequestType header value.
     */
    String getRestTypeValue();

    /**
     * Even request is unauthorized, and RequestType is not REST, the request will be redirected to this URI.
     *
     * @return URI to redirect.
     */
    String getUnauthorizedRedirection();

    /**
     * Even request is forbidden (User does not have the necessary Role), and RequestType is not REST, the request will be redirected to this URI.
     * @return URI to redirect.
     */
    String getForbiddenRedirection();

    /**
     * Name of the Cookie to store AccessTokenId.
     *
     * @return AccessTokenId cookie name.
     */
    String getAccessTokenIdCookie();

    /**
     * Name of the Cookie to store UserId.
     *
     * @return UserId cookie name.
     */
    String getUserIdCookie();

    /**
     * List of URIs that do NOT require authorization and authentication.
     *
     * @return list of allowed URIs
     */
    List<String> getAllowedUris();


    /**
     * If an URI requires special role(s) to access, add to this map.
     * <p>
     * Key: Ant pattern of the URI.
     * Value: Set of Roles. The user has to have at least one of these roles to access the URI.
     * <p>
     * Example: Endpoints start with "admin/" can only be access with role "ADMIN".
     * Key: admin/**
     * Value: [ADMIN]
     * <p>
     * Example: Endpoints start with "user/" can be access with role "USER" and "ADMIN", but "VISITOR" cannot.
     * * Key: user/**
     * * Value: [ADMIN, USER]
     *
     * @return Map of URI patterns and Necessary Role(s) to access them.
     */
    Map<String, Set<Role>> getRoleSettings();

    /**
     * If multiple login is allowed, the user can log in from many devices at the same time.
     * If it is disabled, logging in from a second device will delete the access token belongs to the first device.
     *
     * @return true, if multiple login is allowed, false if not.
     */
    boolean isMultipleLoginAllowed();

    /**
     * AccessToken expires after X minutes.
     *
     * @return AccessToken expiration in minutes.
     */
    long getTokenExpirationMinutes();
}
