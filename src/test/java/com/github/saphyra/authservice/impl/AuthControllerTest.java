package com.github.saphyra.authservice.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.AuthContext;
import com.github.saphyra.authservice.domain.LoginRequest;
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
    private static final String REQUEST_URI = "request_uri";

    @Mock
    private AuthService authService;

    @Mock
    private PropertyConfiguration propertyConfiguration;

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

    @Captor
    private ArgumentCaptor<AuthContext> argumentCaptor;

    @Before
    public void init() {
        when(propertyConfiguration.getAccessTokenIdCookie()).thenReturn(COOKIE_ACCESS_TOKEN_ID);
        when(propertyConfiguration.getUserIdCookie()).thenReturn(COOKIE_USER_ID);
        when(propertyConfiguration.getSuccessfulLoginRedirection()).thenReturn(LOGIN_REDIRECTION);
        when(propertyConfiguration.getLogoutRedirection()).thenReturn(LOGOUT_REDIRECTION);

        given(request.getRequestURI()).willReturn(REQUEST_URI);
        given(request.getMethod()).willReturn(HttpMethod.POST.name());

        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));
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
        verify(filterHelper).handleAccessDenied(eq(request), eq(response), argumentCaptor.capture());
        verifyAuthContext();
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
        verify(filterHelper).handleAccessDenied(eq(request), eq(response), argumentCaptor.capture());
        verifyAuthContext();
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
        when(propertyConfiguration.getLogoutRedirection()).thenReturn(null);
        //WHEN
        underTest.logout(request, response);
        //THEN
        verify(authService).logout(USER_ID, ACCESS_TOKEN_ID);
        verifyZeroInteractions(response);
    }

    private void verifyAuthContext() {
        AuthContext authContext = argumentCaptor.getValue();
        assertThat(authContext.getRequestUri()).isEqualTo(REQUEST_URI);
        assertThat(authContext.getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(authContext.getAccessTokenId()).contains(ACCESS_TOKEN_ID);
        assertThat(authContext.getUserId()).contains(USER_ID);
        assertThat(authContext.getAccessStatus()).isEqualTo(AccessStatus.UNAUTHORIZED);
    }
}