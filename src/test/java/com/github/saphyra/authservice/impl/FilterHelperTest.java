package com.github.saphyra.authservice.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.saphyra.authservice.ErrorResponseResolver;
import com.github.saphyra.authservice.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.domain.AuthContext;
import com.github.saphyra.authservice.domain.RestErrorResponse;

@RunWith(MockitoJUnitRunner.class)
public class FilterHelperTest {
    private static final String REQUEST_TYPE_HEADER = "header";
    private static final String REST_TYPE_REQUEST = "rest";
    private static final String REDIRECTION_PATH = "redirection";
    private static final String RESPONSE_BODY = "response_body";

    @Mock
    private ErrorResponseResolver errorResponseResolver;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PropertyConfiguration propertyConfiguration;

    @InjectMocks
    private FilterHelper underTest;

    @Mock
    private AuthContext authContext;

    @Mock
    private PrintWriter writer;

    @Before
    public void init() {
        when(propertyConfiguration.getRequestTypeHeader()).thenReturn(REQUEST_TYPE_HEADER);
        when(propertyConfiguration.getRestTypeValue()).thenReturn(REST_TYPE_REQUEST);
    }

    @Test
    public void testHandleUnauthorizedShouldSendErrorWhenUnauthorizedRest() throws IOException {
        //GIVEN
        when(request.getHeader(REQUEST_TYPE_HEADER)).thenReturn(REST_TYPE_REQUEST);

        RestErrorResponse restErrorResponse = new RestErrorResponse(HttpStatus.BAD_REQUEST, RESPONSE_BODY);
        given(errorResponseResolver.getRestErrorResponse(authContext)).willReturn(restErrorResponse);
        given(objectMapper.writeValueAsString(RESPONSE_BODY)).willReturn(RESPONSE_BODY);

        given(response.getWriter()).willReturn(writer);
        //WHEN
        underTest.handleAccessDenied(request, response, authContext);
        //THEN
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
        verify(writer).write(RESPONSE_BODY);
        verify(writer).flush();
        verify(writer).close();
    }

    @Test
    public void testHandleUnauthorizedShouldRedirectWhenUnauthorizedNotRest() throws IOException {
        //GIVEN
        when(request.getHeader(REQUEST_TYPE_HEADER)).thenReturn(null);
        given(errorResponseResolver.getRedirectionPath(authContext)).willReturn(REDIRECTION_PATH);
        //WHEN
        underTest.handleAccessDenied(request, response, authContext);
        //THEN
        verify(response).sendRedirect(REDIRECTION_PATH);
    }
}