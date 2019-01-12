package com.github.saphyra.authservice.filter;

import com.github.saphyra.authservice.AuthService;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AllowedUri;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

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
    private PropertySource propertySource;

    @Mock
    private AntPathMatcher antPathMatcher;

    @InjectMocks
    private AuthFilter underTest;

    @Before
    public void init() {
        when(propertySource.getAllowedUris()).thenReturn(Arrays.asList(new AllowedUri(ALLOWED_URI, HttpMethod.POST)));
        when(propertySource.getUserIdCookie()).thenReturn(COOKIE_USER_ID);
        when(propertySource.getAccessTokenIdCookie()).thenReturn(COOKIE_ACCESS_TOKEN_ID);

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

        Cookie[] cookies = new Cookie[]{new Cookie(COOKIE_USER_ID, USER_ID), new Cookie(COOKIE_ACCESS_TOKEN_ID, ACCESS_TOKEN_ID)};
        when(request.getCookies()).thenReturn(cookies);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(authService).canAccess(ALLOWED_URI, USER_ID, ACCESS_TOKEN_ID);
        verifyZeroInteractions(filterChain);
    }

    @Test
    public void testAuthenticationShouldNotCallFacadeWhenCookieNotFound() throws ServletException, IOException {
        //GIVEN
        when(request.getCookies()).thenReturn(null);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verifyNoMoreInteractions(authService);
        verifyNoMoreInteractions(filterChain);
        verify(filterHelper).handleUnauthorized(request, response, AccessStatus.UNAUTHORIZED);
    }

    @Test
    public void testAuthenticationShouldCallFacadeAndFilterChain() throws ServletException, IOException {
        //GIVEN
        Cookie[] cookies = new Cookie[]{new Cookie(COOKIE_USER_ID, USER_ID), new Cookie(COOKIE_ACCESS_TOKEN_ID, ACCESS_TOKEN_ID)};
        when(request.getCookies()).thenReturn(cookies);
        when(authService.canAccess(PROTECTED_URI, USER_ID, ACCESS_TOKEN_ID)).thenReturn(AccessStatus.GRANTED);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(authService).canAccess(PROTECTED_URI, USER_ID, ACCESS_TOKEN_ID);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void testShouldCallFilterHelperWhenNotAuthenticated() throws ServletException, IOException {
        //GIVEN
        Cookie[] cookies = new Cookie[]{new Cookie(COOKIE_USER_ID, USER_ID), new Cookie(COOKIE_ACCESS_TOKEN_ID, ACCESS_TOKEN_ID)};
        when(request.getCookies()).thenReturn(cookies);
        when(authService.canAccess(PROTECTED_URI, USER_ID, ACCESS_TOKEN_ID)).thenReturn(AccessStatus.FORBIDDEN);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(authService).canAccess(PROTECTED_URI, USER_ID, ACCESS_TOKEN_ID);
        verify(filterHelper).handleUnauthorized(request, response, AccessStatus.FORBIDDEN);
        verifyNoMoreInteractions(filterChain);
    }
}