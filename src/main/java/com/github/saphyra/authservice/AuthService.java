package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AccessToken;
import org.springframework.http.HttpMethod;

public interface AuthService {
    /**
     * Determinates if the user is granted to reach the given endpoint.
     * For grant the user has to have a valid AccessToken and the required Role.
     *
     * @param requestUri    Uri of the request used for authentication.
     * @param method        Http method of the request.
     * @param userId        Id of the user (Stored in cookie).
     * @param accessTokenId Id of the user's AccessToken (Stored in cookie).
     * @return true, if the user has access to the endpoint, false otherwise.
     */
    AccessStatus canAccess(String requestUri, HttpMethod method, String userId, String accessTokenId);

    /**
     * Creates new AccessToken for the user, if credentials are correct.
     *
     * @param userName   username entered by user.
     * @param password   password entered by the user.
     * @param rememberMe if true, AccessToken does not expire.
     * @return new AccessToken.
     * @throws com.github.saphyra.exceptionhandling.exception.UnauthorizedException when bad credentials.
     */
    AccessToken login(String userName, String password, Boolean rememberMe);

    /**
     * Deletes the given AccessToken of the user.
     *
     * @param userId        Id of the user (Stored in cookie).
     * @param accessTokenId Id of the AccessToken of user (Stored in cookie).
     */
    void logout(String userId, String accessTokenId);
}
