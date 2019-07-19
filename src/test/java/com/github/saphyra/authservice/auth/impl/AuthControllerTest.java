package com.github.saphyra.authservice.auth.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.saphyra.authservice.auth.AuthService;
import com.github.saphyra.authservice.auth.configuration.AuthPropertyConfiguration;
import com.github.saphyra.authservice.auth.domain.AccessStatus;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.auth.domain.LoginRequest;
import com.github.saphyra.authservice.common.CommonPropertyConfiguration;
import com.github.saphyra.exceptionhandling.exception.ForbiddenException;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import com.github.saphyra.util.CookieUtil;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String USER_ID = "user_id";
    private static final String COOKIE_ACCESS_TOKEN_ID = "cookie_access_token_id";
    private static final String COOKIE_USER_ID = "cookie_user_id";
    private static final String LOGIN_REDIRECTION = "login_redirection";
    private static final String LOGOUT_REDIRECTION = "logout_redirection";

    @Mock
    private AuthContextFactory authContextFactory;

    @Mock
    private AuthService authService;

    @Mock
    private CommonPropertyConfiguration commonPropertyConfiguration;

    @Mock
    private AuthPropertyConfiguration authPropertyConfiguration;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpServletRequest request;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private FilterHelper filterHelper;

    @InjectMocks
    private AuthController underTest;

    @Mock
    private AuthContext authContext;

    @Before
    public void init() {
        when(commonPropertyConfiguration.getAccessTokenIdCookie()).thenReturn(COOKIE_ACCESS_TOKEN_ID);
        when(commonPropertyConfiguration.getUserIdCookie()).thenReturn(COOKIE_USER_ID);
        when(authPropertyConfiguration.getSuccessfulLoginRedirection()).thenReturn(LOGIN_REDIRECTION);
        when(authPropertyConfiguration.getLogoutRedirection()).thenReturn(LOGOUT_REDIRECTION);

        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));

        given(authContextFactory.create(request, AccessStatus.UNAUTHORIZED)).willReturn(authContext);
        given(authContextFactory.create(request, AccessStatus.FORBIDDEN)).willReturn(authContext);
    }

    @Test
    public void testLoginByRest_successful() throws IOException {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);

        AccessToken accessToken = new AccessToken(ACCESS_TOKEN_ID, USER_ID, false, OffsetDateTime.now());
        when(authService.login(USERNAME, PASSWORD, loginRequest.getRememberMe())).thenReturn(accessToken);
        //WHEN
        underTest.loginByRest(loginRequest, request, response);
        //THEN
        verify(authService).login(USERNAME, PASSWORD, loginRequest.getRememberMe());
        verify(cookieUtil).setCookie(response, COOKIE_USER_ID, USER_ID, -1);
        verify(cookieUtil).setCookie(response, COOKIE_ACCESS_TOKEN_ID, ACCESS_TOKEN_ID, -1);
    }

    @Test
    public void loginByRest_fail() throws IOException {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);
        given(authService.login(USERNAME, PASSWORD, false)).willThrow(new UnauthorizedException(""));
        //WHEN
        underTest.loginByRest(loginRequest, request, response);
        //THEN
        verify(filterHelper).handleAccessDenied(request, response, authContext);
    }

    @Test
    public void testLoginByForm_successful() throws IOException {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);

        AccessToken accessToken = new AccessToken(ACCESS_TOKEN_ID, USER_ID, false, OffsetDateTime.now());
        when(authService.login(USERNAME, PASSWORD, loginRequest.getRememberMe())).thenReturn(accessToken);
        //WHEN
        underTest.loginByForm(loginRequest, request, response);
        //THEN
        verify(authService).login(USERNAME, PASSWORD, loginRequest.getRememberMe());
        verify(cookieUtil).setCookie(response, COOKIE_USER_ID, USER_ID, -1);
        verify(cookieUtil).setCookie(response, COOKIE_ACCESS_TOKEN_ID, ACCESS_TOKEN_ID, -1);
        verify(response).sendRedirect(LOGIN_REDIRECTION);
    }

    @Test
    public void testLoginByForm_Fail() throws IOException {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);

        when(authService.login(USERNAME, PASSWORD, loginRequest.getRememberMe())).thenThrow(new UnauthorizedException("loginFailure"));
        //WHEN
        underTest.loginByForm(loginRequest, request, response);
        //THEN
        verify(filterHelper).handleAccessDenied(request, response, authContext);
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
    public void logout_handleForbidden() throws IOException {
        //GIVEN
        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));

        doThrow(new ForbiddenException("")).when(authService).logout(USER_ID, ACCESS_TOKEN_ID);
        //WHEN
        underTest.logout(request, response);
        //THEN
        verify(filterHelper).handleAccessDenied(request, response, authContext);
    }

    @Test
    public void testLogoutShouldNotRedirect() throws IOException {
        //GIVEN
        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));
        when(authPropertyConfiguration.getLogoutRedirection()).thenReturn(null);
        //WHEN
        underTest.logout(request, response);
        //THEN
        verify(authService).logout(USER_ID, ACCESS_TOKEN_ID);
        verifyZeroInteractions(response);
    }
}