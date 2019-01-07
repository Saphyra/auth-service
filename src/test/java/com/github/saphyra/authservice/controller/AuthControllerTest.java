package com.github.saphyra.authservice.controller;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.controller.request.LoginRequest;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String USER_ID = "user_id";
    private static final String COOKIE_ACCESS_TOKEN_ID = "cookie_access_token_id";
    private static final String COOKIE_USER_ID = "cookie_user_id";
    private static final String LOGIN_REDIRECTION = "login_redirection";
    private static final String UNAUTHORIZED_REDIRECTION = "unauthorized_redirection";

    @Mock
    private AuthService authService;

    @Mock
    private PropertySource propertySource;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthController underTest;

    @Before
    public void init() {
        when(propertySource.getAccessTokenIdCookie()).thenReturn(COOKIE_ACCESS_TOKEN_ID);
        when(propertySource.getUserIdCookie()).thenReturn(COOKIE_USER_ID);
        when(propertySource.getSuccessfulLoginRedirection()).thenReturn(LOGIN_REDIRECTION);
        when(propertySource.getUnauthorizedRedirection()).thenReturn(UNAUTHORIZED_REDIRECTION);
    }

    @Test
    public void testLoginByRest() {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);

        AccessToken accessToken = new AccessToken(ACCESS_TOKEN_ID, USER_ID, false, OffsetDateTime.now());
        when(authService.login(USERNAME, PASSWORD, loginRequest.getRememberMe())).thenReturn(accessToken);
        //WHEN
        underTest.loginByRest(loginRequest, response);
        //THEN
        verify(authService).login(USERNAME, PASSWORD, loginRequest.getRememberMe());
        verify(response, times(2)).addCookie(any(Cookie.class));
    }

    @Test
    public void testLoginByForm() throws IOException {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);

        AccessToken accessToken = new AccessToken(ACCESS_TOKEN_ID, USER_ID, false, OffsetDateTime.now());
        when(authService.login(USERNAME, PASSWORD, loginRequest.getRememberMe())).thenReturn(accessToken);
        //WHEN
        underTest.loginByForm(loginRequest, response);
        //THEN
        verify(authService).login(USERNAME, PASSWORD, loginRequest.getRememberMe());
        verify(response, times(2)).addCookie(any(Cookie.class));
        verify(response).sendRedirect(LOGIN_REDIRECTION);
    }

    @Test
    public void testLoginByFormFail() throws IOException {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);

        when(authService.login(USERNAME, PASSWORD, loginRequest.getRememberMe())).thenThrow(new UnauthorizedException("loginFailure"));
        //WHEN
        underTest.loginByForm(loginRequest, response);
        //THEN
        verify(authService).login(USERNAME, PASSWORD, loginRequest.getRememberMe());
        verify(response).sendRedirect(UNAUTHORIZED_REDIRECTION + "?" + AuthController.UNAUTHORIZED_PARAM);
    }

    @Test
    public void testLogout() {
        //GIVEN
        Cookie[] cookies = new Cookie[]{new Cookie(COOKIE_USER_ID, USER_ID), new Cookie(COOKIE_ACCESS_TOKEN_ID, ACCESS_TOKEN_ID)};
        when(request.getCookies()).thenReturn(cookies);
        //WHEN
        underTest.logout(request);
        //THEN
        verify(authService).logout(USER_ID, ACCESS_TOKEN_ID);
    }
}