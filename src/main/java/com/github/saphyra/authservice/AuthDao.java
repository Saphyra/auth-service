package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.User;

import java.time.OffsetDateTime;
import java.util.Optional;

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
}
