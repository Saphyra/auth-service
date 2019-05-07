package com.github.saphyra.authservice;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.github.saphyra.authservice.domain.AllowedUri;
import com.github.saphyra.authservice.domain.RoleSetting;

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
     *
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
     * List of URIs and Http methods that do NOT require authorization and authentication.
     *
     * @return list of allowed URIs
     */
    default List<AllowedUri> getAllowedUris() {
        return Collections.emptyList();
    }

    /**
     * List of URIs and Http methods that do NOT extend the user's session when called.
     * @return list of non-extending uris
     */
    default List<AllowedUri> getNonSessionExtendingUris(){
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
     * * uri: user/**
     * protectedMethods [HttpMethod.values()]
     * * protectedMethods: [ADMIN, USER]
     *
     * @return Map of URI patterns and Necessary Role(s) to access them.
     */
    default Set<RoleSetting> getRoleSettings() {
        return new HashSet<>();
    }

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

    /**
     * This value determinates the filter's place in filter chain. The lower the value, the earlier the filter runs.
     *
     * @return filter order.
     */
    int getFilterOrder();

    /**
     * If the login was successful and sent by HTML form, service will redirect to this URI.
     *
     * @return Redirection path
     */
    String getSuccessfulLoginRedirection();

    /**
     * If the logout was successful, service will redirect to this URI.
     *
     * @return Redirection path, or empty if no redirect wanted.
     */
    default Optional<String> getLogoutRedirection() {
        return Optional.empty();
    }
}
