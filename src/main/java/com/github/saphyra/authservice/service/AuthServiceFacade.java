package com.github.saphyra.authservice.service;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceFacade implements AuthService {
    private final AccessService accessService;
    private final LoginService loginService;
    private final LogoutService logoutService;

    @Override
    public AccessStatus canAccess(String requestUri, HttpMethod method, String userId, String accessTokenId) {
        return accessService.canAccess(requestUri, method, userId, accessTokenId);
    }

    @Override
    public AccessToken login(String userName, String password, Boolean rememberMe) {
        return loginService.login(userName, password, rememberMe);
    }

    @Override
    public void logout(String userId, String accessTokenId) {
        logoutService.logout(userId, accessTokenId);
    }
}
