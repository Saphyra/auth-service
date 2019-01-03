package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.User;

import java.util.Optional;

public interface AuthDao {
    Optional<User> findUserById(String userId);

    Optional<User> findUserByUserName(String userName);

    void deleteAccessToken(AccessToken accessToken);

    void deleteAccessTokenByUserId(String userId);

    AccessToken findAccessTokenByTokenId(String key);

    void saveAccessToken(AccessToken accessToken);
}
