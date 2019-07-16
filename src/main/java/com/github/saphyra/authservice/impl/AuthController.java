package com.github.saphyra.authservice.impl;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.LoginRequest;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import com.github.saphyra.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
class AuthController {
    static final String LOGIN_MAPPING = "login";
    static final String LOGOUT_MAPPING = "logout";
    static final String UNAUTHORIZED_PARAM = "loginFailure=unauthorized";

    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final PropertyConfiguration propertyConfiguration;

    @PostMapping(value = LOGIN_MAPPING, consumes = MediaType.APPLICATION_JSON_VALUE)
    void loginByRest(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        log.info("Login request arrived to REST.");
        login(loginRequest, response);
    }

    @PostMapping(value = LOGIN_MAPPING, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void loginByForm(@Valid LoginRequest loginRequest, HttpServletResponse response) throws IOException {
        log.info("Login request arrived to FORM.");
        try {
            login(loginRequest, response);
            response.sendRedirect(propertyConfiguration.getSuccessfulLoginRedirection());
        } catch (UnauthorizedException ex) {
            response.sendRedirect(String.format("%s?%s", propertyConfiguration.getUnauthorizedLoginRedirection(), UNAUTHORIZED_PARAM));
        }
    }

    private void login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        AccessToken accessToken = authService.login(loginRequest.getUserName(), loginRequest.getPassword(), loginRequest.getRememberMe());
        int expiration = accessToken.isPersistent() ? Integer.MAX_VALUE : -1;
        cookieUtil.setCookie(response, propertyConfiguration.getUserIdCookie(), accessToken.getUserId(), expiration);
        cookieUtil.setCookie(response, propertyConfiguration.getAccessTokenIdCookie(), accessToken.getAccessTokenId(), expiration);
        log.info("Access token successfully created, and sent for the client.");
    }

    @RequestMapping(LOGOUT_MAPPING)
    void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("Logout request arrived.");
        Optional<String> userId = cookieUtil.getCookie(request, propertyConfiguration.getUserIdCookie());
        Optional<String> accessTokenId = cookieUtil.getCookie(request, propertyConfiguration.getAccessTokenIdCookie());
        if (userId.isPresent() && accessTokenId.isPresent()) {
            authService.logout(userId.get(), accessTokenId.get());
        }

        Optional.ofNullable(propertyConfiguration.getLogoutRedirection()).ifPresent(s -> {
            try {
                response.sendRedirect(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
