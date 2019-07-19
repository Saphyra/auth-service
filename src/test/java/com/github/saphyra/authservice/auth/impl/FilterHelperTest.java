package com.github.saphyra.authservice.auth.impl;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import com.github.saphyra.authservice.auth.ErrorResponseResolver;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.auth.domain.RestErrorResponse;
import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.util.ObjectMapperWrapper;

@RunWith(MockitoJUnitRunner.class)
public class FilterHelperTest {
    private static final String REDIRECTION_PATH = "redirection";
    private static final String RESPONSE_BODY = "response_body";

    @Mock
    private ErrorResponseResolver errorResponseResolver;

    @Mock
    private ObjectMapperWrapper objectMapperWrapper;

    @Mock
    private RequestHelper requestHelper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private FilterHelper underTest;

    @Mock
    private AuthContext authContext;

    @Mock
    private PrintWriter writer;

    @Test
    public void testHandleUnauthorizedShouldSendErrorWhenUnauthorizedRest() throws IOException {
        //GIVEN
        given(requestHelper.isRestCall(request)).willReturn(true);

        RestErrorResponse restErrorResponse = new RestErrorResponse(HttpStatus.BAD_REQUEST, RESPONSE_BODY);
        given(errorResponseResolver.getRestErrorResponse(authContext)).willReturn(restErrorResponse);
        given(objectMapperWrapper.writeValueAsString(RESPONSE_BODY)).willReturn(RESPONSE_BODY);

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
        given(requestHelper.isRestCall(request)).willReturn(false);
        given(errorResponseResolver.getRedirectionPath(authContext)).willReturn(REDIRECTION_PATH);
        //WHEN
        underTest.handleAccessDenied(request, response, authContext);
        //THEN
        verify(response).sendRedirect(REDIRECTION_PATH);
    }
}