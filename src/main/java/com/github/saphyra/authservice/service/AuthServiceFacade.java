package com.github.saphyra.authservice.service;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.domain.AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
//TODO unit test
public class AuthServiceFacade implements AuthService {
    private final LoginService loginService;
    private final LogoutService logoutService;

    @Override
    public boolean canAccess(String requestUri, String userId, String accessTokenId) {
        return false;
    }

    @Override
    public AccessToken login(String userName, String password) {
        return loginService.login(userName, password);
    }

    @Override
    public void logout(String userId, String accessTokenId) {
        logoutService.logout(userId, accessTokenId);
    }
}
