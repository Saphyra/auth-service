package com.github.saphyra.authservice.impl;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.LoginRequest;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import com.github.saphyra.util.CookieUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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
    private static final String LOGOUT_REDIRECTION = "logout_redirection";

    @Mock
    private AuthService authService;

    @Mock
    private PropertySource propertySource;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CookieUtil cookieUtil;

    @InjectMocks
    private AuthController underTest;

    @Before
    public void init() {
        when(propertySource.getAccessTokenIdCookie()).thenReturn(COOKIE_ACCESS_TOKEN_ID);
        when(propertySource.getUserIdCookie()).thenReturn(COOKIE_USER_ID);
        when(propertySource.getSuccessfulLoginRedirection()).thenReturn(LOGIN_REDIRECTION);
        when(propertySource.getUnauthorizedRedirection()).thenReturn(UNAUTHORIZED_REDIRECTION);
        when(propertySource.getLogoutRedirection()).thenReturn(Optional.of(LOGOUT_REDIRECTION));
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
        verify(cookieUtil).setCookie(response, COOKIE_USER_ID, USER_ID, -1);
        verify(cookieUtil).setCookie(response, COOKIE_ACCESS_TOKEN_ID, ACCESS_TOKEN_ID, -1);
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
        verify(cookieUtil).setCookie(response, COOKIE_USER_ID, USER_ID, -1);
        verify(cookieUtil).setCookie(response, COOKIE_ACCESS_TOKEN_ID, ACCESS_TOKEN_ID, -1);
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
    public void testLogout() throws IOException {
        //GIVEN
        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));
        //WHEN
        underTest.logout(request, response);
        //THEN
        verify(authService).logout(USER_ID, ACCESS_TOKEN_ID);
        verify(response).sendRedirect(LOGOUT_REDIRECTION);
    }

    @Test
    public void testLogoutShouldNotRedirect() {
        //GIVEN
        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));
        when(propertySource.getLogoutRedirection()).thenReturn(Optional.empty());
        //WHEN
        underTest.logout(request, response);
        //THEN
        verify(authService).logout(USER_ID, ACCESS_TOKEN_ID);
        verifyZeroInteractions(response);
    }
}