package com.github.saphyra.authservice.integration.cases.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.ErrorResponseResolver;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AuthContext;
import com.github.saphyra.authservice.domain.Credentials;
import com.github.saphyra.authservice.domain.LoginRequest;
import com.github.saphyra.authservice.domain.RestErrorResponse;
import com.github.saphyra.authservice.domain.User;
import com.github.saphyra.authservice.integration.component.MockMvcWrapper;
import com.github.saphyra.authservice.integration.component.ResponseValidator;
import com.github.saphyra.authservice.integration.configuration.MvcConfiguration;
import com.github.saphyra.authservice.integration.domain.UrlEncodedLoginRequest;
import com.github.saphyra.exceptionhandling.domain.ErrorResponse;
import com.github.saphyra.util.ObjectMapperWrapper;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {MvcConfiguration.class, FailedLoginTest.class})
public class FailedLoginTest {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CREDENTIALS_PASSWORD = "credentials_password";
    private static final String USER_ID = "user_id";
    private static final String LOGIN_MAPPING = "/login";
    private static final String LOGIN_FAILURE_REDIRECTION = "/login_failure_redirection";

    @Autowired
    private MockMvcWrapper mockMvcWrapper;

    @Autowired
    private AuthDao authDao;

    @Autowired
    private ObjectMapperWrapper objectMapperWrapper;

    @Autowired
    private ErrorResponseResolver errorResponseResolver;

    @Autowired
    private ResponseValidator responseValidator;

    private LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);
    private UrlEncodedLoginRequest urlEncodedLoginRequest = new UrlEncodedLoginRequest(loginRequest);

    @Captor
    private ArgumentCaptor<AuthContext> argumentCaptor;

    @Before
    public void setUp() {
        Credentials credentials = Credentials.builder()
            .password(CREDENTIALS_PASSWORD)
            .build();
        User user = User.builder()
            .userId(USER_ID)
            .credentials(credentials)
            .build();

        given(authDao.findUserByUserName(USERNAME)).willReturn(Optional.of(user));
        given(authDao.authenticate(PASSWORD, CREDENTIALS_PASSWORD)).willReturn(true);

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus(HttpStatus.UNAUTHORIZED.value());
        errorResponse.setErrorCode(AccessStatus.UNAUTHORIZED.name());
        RestErrorResponse restErrorResponse = new RestErrorResponse(HttpStatus.UNAUTHORIZED, errorResponse);
        given(errorResponseResolver.getRestErrorResponse(any())).willReturn(restErrorResponse);

        given(errorResponseResolver.getRedirectionPath(any())).willReturn(LOGIN_FAILURE_REDIRECTION);
    }

    @Test
    public void loginByRest_wrongUserName() throws Exception {
        //GIVEN
        given(authDao.findUserByUserName(USERNAME)).willReturn(Optional.empty());
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(LOGIN_MAPPING, true, loginRequest);
        //THEN
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        AuthContext authContext = argumentCaptor.getValue();

        verifyAuthContext(authContext, true);
        verifyUnauthorizedRestResponse(response);
    }

    @Test
    public void loginByRest_wrongPassword() throws Exception {
        //GIVEN
        given(authDao.authenticate(PASSWORD, CREDENTIALS_PASSWORD)).willReturn(false);
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(LOGIN_MAPPING, true, loginRequest);
        //THEN
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        AuthContext authContext = argumentCaptor.getValue();

        verifyAuthContext(authContext, true);
        verifyUnauthorizedRestResponse(response);
    }

    @Test
    public void loginByForm_wrongUserName() throws Exception {
        //GIVEN
        given(authDao.findUserByUserName(USERNAME)).willReturn(Optional.empty());
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(LOGIN_MAPPING, false, urlEncodedLoginRequest);
        //THEN
        verify(errorResponseResolver).getRedirectionPath(argumentCaptor.capture());
        AuthContext authContext = argumentCaptor.getValue();

        verifyAuthContext(authContext, false);
        responseValidator.verifyRedirection(response, LOGIN_FAILURE_REDIRECTION);
    }

    @Test
    public void loginByForm_wrongPassword() throws Exception {
        //GIVEN
        given(authDao.authenticate(PASSWORD, CREDENTIALS_PASSWORD)).willReturn(false);
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(LOGIN_MAPPING, false, urlEncodedLoginRequest);
        //THEN
        verify(errorResponseResolver).getRedirectionPath(argumentCaptor.capture());
        AuthContext authContext = argumentCaptor.getValue();

        verifyAuthContext(authContext, false);
        responseValidator.verifyRedirection(response, LOGIN_FAILURE_REDIRECTION);
    }

    private void verifyAuthContext(AuthContext authContext, boolean isRest) {
        assertThat(authContext.getAccessStatus()).isEqualTo(AccessStatus.UNAUTHORIZED);
        assertThat(authContext.getRequestUri()).isEqualTo(LOGIN_MAPPING);
        assertThat(authContext.getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(authContext.isRest()).isEqualTo(isRest);
        assertThat(authContext.getAccessTokenId()).isEmpty();
        assertThat(authContext.getUserId()).isEmpty();
    }

    private void verifyUnauthorizedRestResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        ErrorResponse errorResponse = objectMapperWrapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(errorResponse.getErrorCode()).isEqualTo(AccessStatus.UNAUTHORIZED.name());
    }
}
