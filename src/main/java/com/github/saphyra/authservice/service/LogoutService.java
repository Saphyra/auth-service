package com.github.saphyra.authservice.service;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.exceptionhandling.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutService {
    private final AccessTokenCache accessTokenCache;
    private final AuthDao authDao;

    //TODO unit test
    public void logout(String userId, String accessTokenId) {
        accessTokenCache.get(accessTokenId).ifPresent(accessToken -> {
            if (accessToken.getUserId().equals(userId)) {
                accessTokenCache.invalidate(accessTokenId);
                authDao.deleteAccessToken(accessToken);
            } else throw new ForbiddenException(userId + " has no access to accessToken " + accessTokenId);
        });
    }
}
