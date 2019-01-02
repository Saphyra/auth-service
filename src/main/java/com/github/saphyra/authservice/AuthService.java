package com.github.saphyra.authservice;

public interface AuthService {
    boolean isAuthenticated(String userId, String accessTokenId);
}
