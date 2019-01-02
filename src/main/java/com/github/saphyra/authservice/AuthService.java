package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.AccessToken;

import java.util.Optional;

public interface AuthService {
    boolean isAuthenticated(String userId, String accessTokenId);

    AccessToken login(String userName, String password);

    void logout(Optional<String> userId, Optional<String> accessTokenId);
}
