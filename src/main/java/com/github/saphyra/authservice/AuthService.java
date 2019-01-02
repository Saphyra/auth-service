package com.github.saphyra.authservice;

import com.github.saphyra.authservice.domain.AccessToken;

public interface AuthService {
    boolean canAccess(String requestUri, String userId, String accessTokenId);

    AccessToken login(String userName, String password);

    void logout(String userId, String accessTokenId);
}
