package com.github.saphyra.authservice.integration.cases.logout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
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
import com.github.saphyra.authservice.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.AuthContext;
import com.github.saphyra.authservice.domain.RestErrorResponse;
import com.github.saphyra.authservice.integration.component.MockMvcWrapper;
import com.github.saphyra.authservice.integration.configuration.MvcConfiguration;
import com.github.saphyra.exceptionhandling.domain.ErrorResponse;
import com.github.saphyra.util.ObjectMapperWrapper;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {MvcConfiguration.class, LogoutWithRestTest.class})
public class LogoutWithRestTest {
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String USER_ID = "user_id";
    private static final String FAKE_USER_ID = "fake_user_id";
    private static final String LOGOUT_MAPPING = "/logout";

    @Autowired
    private MockMvcWrapper mockMvcWrapper;

    @Autowired
    private AuthDao authDao;

    @Autowired
    private PropertyConfiguration propertyConfiguration;

    @Autowired
    private ErrorResponseResolver errorResponseResolver;

    @Autowired
    private ObjectMapperWrapper objectMapperWrapper;

    @Mock
    private AccessToken accessToken;

    @Before
    public void setUp() {
        given(accessToken.getUserId()).willReturn(USER_ID);
    }

    @Test
    public void logout() throws Exception {
        //GIVEN
        Cookie accessTokenCookie = createCookie(propertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID);
        Cookie userIdCookie = createCookie(propertyConfiguration.getUserIdCookie(), USER_ID);

        given(authDao.findAccessTokenByTokenId(ACCESS_TOKEN_ID)).willReturn(Optional.of(accessToken));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/logout", true, null, accessTokenCookie, userIdCookie);
        //THEN
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(authDao).deleteAccessToken(accessToken);
        verify(authDao).successfulLogoutCallback(accessToken);
    }

    @Test
    public void logoutWhenNotLoggedIn() throws Exception {
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/logout", true, null);
        //THEN
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void logoutWithForbidden() throws Exception {
        //GIVEN
        Cookie accessTokenCookie = createCookie(propertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID);
        Cookie userIdCookie = createCookie(propertyConfiguration.getUserIdCookie(), FAKE_USER_ID);

        given(authDao.findAccessTokenByTokenId(ACCESS_TOKEN_ID)).willReturn(Optional.of(accessToken));

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setHttpStatus(HttpStatus.FORBIDDEN.value());
        errorResponse.setErrorCode(AccessStatus.FORBIDDEN.name());
        RestErrorResponse restErrorResponse = new RestErrorResponse(HttpStatus.FORBIDDEN, errorResponse);
        given(errorResponseResolver.getRestErrorResponse(any())).willReturn(restErrorResponse);
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(LOGOUT_MAPPING, true, null, accessTokenCookie, userIdCookie);
        //THEN
        ArgumentCaptor<AuthContext> argumentCaptor = ArgumentCaptor.forClass(AuthContext.class);
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        AuthContext authContext = argumentCaptor.getValue();
        verifyAuthContext(authContext);

        verifyForbiddenRestResponse(response);
    }

    private void verifyForbiddenRestResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        ErrorResponse errorResponse = objectMapperWrapper.readValue(response.getContentAsString(), ErrorResponse.class);
        assertThat(errorResponse.getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(errorResponse.getErrorCode()).isEqualTo(AccessStatus.FORBIDDEN.name());
    }

    private void verifyAuthContext(AuthContext authContext) {
        assertThat(authContext.getAccessStatus()).isEqualTo(AccessStatus.FORBIDDEN);
        assertThat(authContext.getRequestUri()).isEqualTo(LOGOUT_MAPPING);
        assertThat(authContext.getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(authContext.isRest()).isTrue();
        assertThat(authContext.getAccessTokenId()).contains(ACCESS_TOKEN_ID);
        assertThat(authContext.getUserId()).contains(FAKE_USER_ID);
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        return cookie;
    }
}
