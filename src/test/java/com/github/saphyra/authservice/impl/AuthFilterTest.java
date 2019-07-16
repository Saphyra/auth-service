package com.github.saphyra.authservice.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.UriConfiguration;
import com.github.saphyra.authservice.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AllowedUri;
import com.github.saphyra.authservice.domain.AuthContext;
import com.github.saphyra.util.CookieUtil;

@RunWith(MockitoJUnitRunner.class)
public class AuthFilterTest {
    private static final String ALLOWED_URI = "allowed";
    private static final String PROTECTED_URI = "protected";
    private static final String COOKIE_USER_ID = "cookie_user_id";
    private static final String USER_ID = "user_id";
    private static final String COOKIE_ACCESS_TOKEN_ID = "cookie_access_token_id";
    private static final String ACCESS_TOKEN_ID = "access_token_id";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private AuthService authService;

    @Mock
    private FilterHelper filterHelper;

    @Mock
    private UriConfiguration uriConfiguration;

    @Mock
    private PropertyConfiguration propertyConfiguration;

    @Mock
    private AntPathMatcher antPathMatcher;

    @Mock
    private CookieUtil cookieUtil;

    @InjectMocks
    private AuthFilter underTest;

    @Before
    public void init() {
        when(uriConfiguration.getAllowedUris()).thenReturn(Arrays.asList(new AllowedUri(ALLOWED_URI, HttpMethod.POST)));
        when(propertyConfiguration.getUserIdCookie()).thenReturn(COOKIE_USER_ID);
        when(propertyConfiguration.getAccessTokenIdCookie()).thenReturn(COOKIE_ACCESS_TOKEN_ID);

        when(request.getRequestURI()).thenReturn(PROTECTED_URI);
        when(request.getMethod()).thenReturn(HttpMethod.POST.name());

        when(antPathMatcher.match(ALLOWED_URI, ALLOWED_URI)).thenReturn(true);

        underTest.mapAllowedUris();
    }

    @Test
    public void testAllowedPathShouldNotCallFacade() throws ServletException, IOException {
        //GIVEN
        when(request.getRequestURI()).thenReturn(ALLOWED_URI);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verifyNoMoreInteractions(authService);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testAllowedPathShouldCallFacadeWhenNotAllowedPath() throws ServletException, IOException {
        //GIVEN
        when(request.getRequestURI()).thenReturn(ALLOWED_URI);
        when(request.getMethod()).thenReturn(HttpMethod.GET.name());

        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(authService).canAccess(ALLOWED_URI, HttpMethod.GET, USER_ID, ACCESS_TOKEN_ID);
        verifyZeroInteractions(filterChain);
    }

    @Test
    public void testAuthenticationShouldNotCallFacadeWhenCookieNotFound() throws ServletException, IOException {
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verifyNoMoreInteractions(authService);
        verifyNoMoreInteractions(filterChain);
        ArgumentCaptor<AuthContext> argumentCaptor = ArgumentCaptor.forClass(AuthContext.class);
        verify(filterHelper).handleAccessDenied(eq(request), eq(response), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getRequestUri()).isEqualTo(PROTECTED_URI);
        assertThat(argumentCaptor.getValue().getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(argumentCaptor.getValue().getAccessTokenId()).isEmpty();
        assertThat(argumentCaptor.getValue().getUserId()).isEmpty();
        assertThat(argumentCaptor.getValue().getAccessStatus()).isEqualTo(AccessStatus.UNAUTHORIZED);
    }

    @Test
    public void testAuthenticationShouldCallFacadeAndFilterChain() throws ServletException, IOException {
        //GIVEN
        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));
        when(authService.canAccess(PROTECTED_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID)).thenReturn(AccessStatus.GRANTED);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(authService).canAccess(PROTECTED_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testShouldCallFilterHelperWhenNotAuthenticated() throws ServletException, IOException {
        //GIVEN
        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));
        when(authService.canAccess(PROTECTED_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID)).thenReturn(AccessStatus.FORBIDDEN);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(authService).canAccess(PROTECTED_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID);

        ArgumentCaptor<AuthContext> argumentCaptor = ArgumentCaptor.forClass(AuthContext.class);
        verify(filterHelper).handleAccessDenied(eq(request), eq(response), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().getRequestUri()).isEqualTo(PROTECTED_URI);
        assertThat(argumentCaptor.getValue().getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(argumentCaptor.getValue().getAccessTokenId()).contains(ACCESS_TOKEN_ID);
        assertThat(argumentCaptor.getValue().getUserId()).contains(USER_ID);
        assertThat(argumentCaptor.getValue().getAccessStatus()).isEqualTo(AccessStatus.FORBIDDEN);
        verifyNoMoreInteractions(filterChain);
    }
}