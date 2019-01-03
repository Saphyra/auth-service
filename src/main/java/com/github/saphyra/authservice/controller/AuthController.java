package com.github.saphyra.authservice.controller;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.controller.request.LoginRequest;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    public static final String LOGIN_MAPPING = "login";
    private static final String LOGOUT_MAPPING = "logout";

    private final AuthService authService;
    private final PropertySource propertySource;

    @PostMapping(LOGIN_MAPPING)
    public void login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Login request arrived.");
        AccessToken accessToken = authService.login(loginRequest.getUserName(), loginRequest.getPassword(), loginRequest.getRememberMe());
        int expiration = accessToken.isPersistent() ? Integer.MAX_VALUE : -1;
        CookieUtil.setCookie(response, propertySource.getUserIdCookie(), accessToken.getUserId(), expiration);
        CookieUtil.setCookie(response, propertySource.getAccessTokenIdCookie(), accessToken.getAccessTokenId(), expiration);
        log.info("Access token successfully created, and sent for the client.");
    }

    @RequestMapping(LOGOUT_MAPPING)
    public void logout(HttpServletRequest request){
        log.info("Logout request arrived.");
        Optional<String> userId = CookieUtil.getCookie(request, propertySource.getUserIdCookie());
        Optional<String> accessTokenId = CookieUtil.getCookie(request, propertySource.getAccessTokenIdCookie());
        if(userId.isPresent() && accessTokenId.isPresent()){
            authService.logout(userId.get(), accessTokenId.get());
        }
    }
}
