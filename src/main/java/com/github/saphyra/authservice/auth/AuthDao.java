package com.github.saphyra.authservice.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.User;

public interface AuthDao {
    /**
     * Query User by userId.
     *
     * @param userId Id of the User.
     * @return User with the given Id or empty when not found.
     */
    Optional<User> findUserById(String userId);

    /**
     * Query User by userName .
     *
     * @param userName userName of the User.
     * @return User with the given userName or empty when not found.
     */
    Optional<User> findUserByUserName(String userName);

    /**
     * Deleted the given AccessToken.
     *
     * @param accessToken AccessToken to delete.
     */
    void deleteAccessToken(AccessToken accessToken);

    /**
     * Deletes all AccessTokens of User with the given Id.
     *
     * @param userId Id of the User.
     */
    void deleteAccessTokenByUserId(String userId);

    /**
     * Deletes all AccessTokens where lastAccess is before expiration AND not persistent.
     *
     * @param expiration expiration date.
     */
    void deleteExpiredAccessTokens(OffsetDateTime expiration);

    /**
     * Queries AccessToken by accessTokenId.
     *
     * @param accessTokenId id of the AccessToken.
     * @return AccessToken with the given accessTokenId or empty if not found.
     */
    Optional<AccessToken> findAccessTokenByTokenId(String accessTokenId);

    /**
     * Saves (or updates) AccessToken.
     *
     * @param accessToken AccessToken to save (or update).
     */
    void saveAccessToken(AccessToken accessToken);


    /**
     * This method is called when the user is logged out successfully, allowing the client to execute cleanup processes.
     *
     * @param deletedAccessToken the deleted accessToken.
     */
    default void successfulLogoutCallback(AccessToken deletedAccessToken) {

    }

    /**
     * This method is called when the user is logged in successfully, allowing the client to execute cleanup processes.
     *
     * @param accessToken the newly created accessToken.
     */
    default void successfulLoginCallback(AccessToken accessToken) {

    }

    /**
     *
     * @param enteredPassword the password entered by the user.
     * @param storedPassword the password stored in the User entity.
     * @return true if the user entered correct password, false otherwise.
     */
    boolean authenticate(String enteredPassword, String storedPassword);
}
