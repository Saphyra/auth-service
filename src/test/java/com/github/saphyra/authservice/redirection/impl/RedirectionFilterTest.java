package com.github.saphyra.authservice.redirection.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.authservice.redirection.RedirectionFilterSettings;
import com.github.saphyra.authservice.redirection.domain.ProtectedUri;
import com.github.saphyra.authservice.redirection.domain.RedirectionContext;

@RunWith(MockitoJUnitRunner.class)
public class RedirectionFilterTest {
    private static final String REQUEST_URI = "request_uri";
    private static final String ALLOWED_URI = "allowed_uri";
    private static final String REDIRECTION_PATH = "redirection_path";

    @Mock
    private RedirectionFilterSettings redirectionFilterSetting;

    @Mock
    private RedirectionContextFactory redirectionContextFactory;

    @Mock
    private RequestHelper requestHelper;

    private RedirectionFilter underTest;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private RedirectionContext redirectionContext;

    @Before
    public void setUp() {
        underTest = RedirectionFilter.builder()
            .antPathMatcher(new AntPathMatcher())
            .redirectionFilterSettings(Arrays.asList(redirectionFilterSetting))
            .redirectionContextFactory(redirectionContextFactory)
            .requestHelper(requestHelper)
            .build();

        given(request.getRequestURI()).willReturn(REQUEST_URI);

        given(requestHelper.getMethod(request)).willReturn(HttpMethod.POST);

        given(redirectionContextFactory.create(request)).willReturn(redirectionContext);
    }

    @Test
    public void notProtectedUri() throws ServletException, IOException {
        //GIVEN
        ProtectedUri protectedUri = new ProtectedUri(ALLOWED_URI, new HashSet<>(Arrays.asList(HttpMethod.POST)));
        given(redirectionFilterSetting.getProtectedUri()).willReturn(protectedUri);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void notProtectedMethod() throws ServletException, IOException {
        //GIVEN
        ProtectedUri protectedUri = new ProtectedUri(REQUEST_URI, new HashSet<>(Arrays.asList(HttpMethod.GET)));
        given(redirectionFilterSetting.getProtectedUri()).willReturn(protectedUri);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void protectedEndpoint_shouldNotRedirect() throws ServletException, IOException {
        //GIVEN
        ProtectedUri protectedUri = new ProtectedUri(REQUEST_URI, new HashSet<>(Arrays.asList(HttpMethod.POST)));
        given(redirectionFilterSetting.getProtectedUri()).willReturn(protectedUri);
        given(redirectionFilterSetting.shouldRedirect(redirectionContext)).willReturn(false);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verify(redirectionFilterSetting).shouldRedirect(redirectionContext);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void protectedEndpoint_shouldRedirect() throws ServletException, IOException {
        //GIVEN
        ProtectedUri protectedUri = new ProtectedUri(REQUEST_URI, new HashSet<>(Arrays.asList(HttpMethod.POST)));
        given(redirectionFilterSetting.getProtectedUri()).willReturn(protectedUri);
        given(redirectionFilterSetting.shouldRedirect(redirectionContext)).willReturn(true);
        given(redirectionFilterSetting.getRedirectionPath(redirectionContext)).willReturn(REDIRECTION_PATH);
        //WHEN
        underTest.doFilterInternal(request, response, filterChain);
        //THEN
        verifyZeroInteractions(filterChain);
        verify(response).sendRedirect(REDIRECTION_PATH);
    }
}