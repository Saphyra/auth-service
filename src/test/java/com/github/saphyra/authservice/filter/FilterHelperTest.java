package com.github.saphyra.authservice.filter;

import com.github.saphyra.authservice.PropertySource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FilterHelperTest {
    private static final String REQUEST_TYPE_HEADER = "header";
    private static final String REST_TYPE_REQUEST = "rest";
    private static final String REDIRECTION_PATH = "redirection";
    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PropertySource propertySource;

    @InjectMocks
    private FilterHelper underTest;

    @Before
    public void init(){
        when(propertySource.getRequestTypeHeader()).thenReturn(REQUEST_TYPE_HEADER);
        when(propertySource.getRestTypeValue()).thenReturn(REST_TYPE_REQUEST);
    }

    @Test
    public void testHandleUnauthorizedShouldSendErrorWhenRest() throws IOException {
        //GIVEN
        when(request.getHeader(REQUEST_TYPE_HEADER)).thenReturn(REST_TYPE_REQUEST);
        //WHEN
        underTest.handleUnauthorized(request, response, REDIRECTION_PATH);
        //THEN
        verify(response).sendError(eq(HttpServletResponse.SC_UNAUTHORIZED), anyString());
    }

    @Test
    public void testHandleUnauthorizedShouldRedirectWhenNotRest() throws IOException {
        //GIVEN
        when(request.getHeader(REQUEST_TYPE_HEADER)).thenReturn(null);
        //WHEN
        underTest.handleUnauthorized(request, response, REDIRECTION_PATH);
        //THEN
        verify(response).sendRedirect(REDIRECTION_PATH);
    }
}