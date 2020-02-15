package com.github.saphyra.authservice.auth.impl;

import com.github.saphyra.authservice.auth.AuthService;
import com.github.saphyra.authservice.auth.configuration.AuthPropertyConfiguration;
import com.github.saphyra.authservice.auth.domain.AccessStatus;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.auth.domain.LoginRequest;
import com.github.saphyra.authservice.common.CommonAuthProperties;
import com.github.saphyra.exceptionhandling.exception.ForbiddenException;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import com.github.saphyra.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;

import static com.github.saphyra.authservice.auth.configuration.AuthPropertyConfiguration.LOGIN_ENDPOINT;
import static com.github.saphyra.authservice.auth.configuration.AuthPropertyConfiguration.LOGOUT_ENDPOINT;

@RestController
@RequiredArgsConstructor
@Slf4j
class AuthController {
    private final AuthContextFactory authContextFactory;
    private final AuthService authService;
    private final CookieUtil cookieUtil;
    private final AuthPropertyConfiguration authPropertyConfiguration;
    private final CommonAuthProperties commonAuthProperties;
    private final FilterHelper filterHelper;

    @PostMapping(value = LOGIN_ENDPOINT, consumes = MediaType.APPLICATION_JSON_VALUE)
    void loginByRest(@RequestBody @Valid LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Login request arrived to REST.");
        log.debug("LoginRequest: {}", loginRequest);
        try {
            login(loginRequest, response);
        } catch (UnauthorizedException ex) {
            log.warn("Login failed: {}", ex.getMessage());
            AuthContext authContext = getAuthContext(request, AccessStatus.LOGIN_FAILED);
            filterHelper.handleAccessDenied(request, response, authContext);
        }
    }

    @PostMapping(value = LOGIN_ENDPOINT, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    void loginByForm(@Valid LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Login request arrived to FORM.");
        log.debug("LoginRequest: {}", loginRequest);
        try {
            login(loginRequest, response);
            response.sendRedirect(authPropertyConfiguration.getSuccessfulLoginRedirection());
        } catch (UnauthorizedException ex) {
            log.warn("Login failed: {}", ex.getMessage());
            AuthContext authContext = getAuthContext(request, AccessStatus.LOGIN_FAILED);
            filterHelper.handleAccessDenied(request, response, authContext);
        }
    }

    private void login(LoginRequest loginRequest, HttpServletResponse response) {
        AccessToken accessToken = authService.login(loginRequest.getUserName(), loginRequest.getPassword(), loginRequest.getRememberMe());
        int expiration = accessToken.isPersistent() ? Integer.MAX_VALUE : -1;
        cookieUtil.setCookie(response, commonAuthProperties.getUserIdCookie(), accessToken.getUserId(), expiration);
        cookieUtil.setCookie(response, commonAuthProperties.getAccessTokenIdCookie(), accessToken.getAccessTokenId(), expiration);
        log.info("Access token successfully created, and sent for the client.");
    }

    @RequestMapping(LOGOUT_ENDPOINT)
    void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Logout request arrived.");
        Optional<String> userId = cookieUtil.getCookie(request, commonAuthProperties.getUserIdCookie());
        Optional<String> accessTokenId = cookieUtil.getCookie(request, commonAuthProperties.getAccessTokenIdCookie());
        try {
            if (userId.isPresent() && accessTokenId.isPresent()) {
                authService.logout(userId.get(), accessTokenId.get());
            }

            Optional.ofNullable(authPropertyConfiguration.getLogoutRedirection()).ifPresent(redirectionPath -> {
                try {
                    response.sendRedirect(redirectionPath);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (ForbiddenException e) {
            log.warn("Logout failed: {}", e.getMessage());
            AuthContext authContext = getAuthContext(request, AccessStatus.FORBIDDEN);
            filterHelper.handleAccessDenied(request, response, authContext);
        }
    }

    private AuthContext getAuthContext(HttpServletRequest request, AccessStatus accessStatus) {
        return authContextFactory.create(request, accessStatus);
    }
}
