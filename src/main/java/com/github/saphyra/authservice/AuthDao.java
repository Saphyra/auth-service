package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.User;

import java.util.Optional;

public interface AuthDao {
    Optional<User> findByUserName(String userName);

    void saveAccessToken(AccessToken accessToken);

    void deleteAccessTokenByUserId(String userId);

    AccessToken findAccessTokenByTokenId(String key);

    void deleteAccessToken(AccessToken accessToken);
}
