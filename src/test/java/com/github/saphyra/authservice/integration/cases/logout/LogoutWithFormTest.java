package com.github.saphyra.authservice.integration.cases.logout;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.ErrorResponseResolver;
import com.github.saphyra.authservice.auth.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.auth.domain.AccessStatus;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.integration.component.MockMvcWrapper;
import com.github.saphyra.authservice.integration.component.ResponseValidator;
import com.github.saphyra.authservice.integration.configuration.MvcConfiguration;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {MvcConfiguration.class, LogoutWithFormTest.class})
@ActiveProfiles("logout_redirection")
public class LogoutWithFormTest {
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String USER_ID = "user_id";
    private static final String FAKE_USER_ID = "fake_user_id";
    private static final String LOGOUT_MAPPING = "/logout";
    private static final String FAILED_LOGOUT_REDIRECTION = "/failed_logout_redirection";

    @Autowired
    private MockMvcWrapper mockMvcWrapper;

    @Autowired
    private AuthDao authDao;

    @Autowired
    private PropertyConfiguration propertyConfiguration;

    @Autowired
    private ErrorResponseResolver errorResponseResolver;

    @Mock
    private AccessToken accessToken;

    @Autowired
    private ResponseValidator responseValidator;

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
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/logout", false, null, accessTokenCookie, userIdCookie);
        //THEN
        responseValidator.verifyRedirection(response, propertyConfiguration.getLogoutRedirection());
        verify(authDao).deleteAccessToken(accessToken);
        verify(authDao).successfulLogoutCallback(accessToken);
    }

    @Test
    public void logoutWhenNotLoggedIn() throws Exception {
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/logout", false, null);
        //THEN
        responseValidator.verifyRedirection(response, propertyConfiguration.getLogoutRedirection());
    }

    @Test
    public void logoutWithForbidden() throws Exception {
        //GIVEN
        Cookie accessTokenCookie = createCookie(propertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID);
        Cookie userIdCookie = createCookie(propertyConfiguration.getUserIdCookie(), FAKE_USER_ID);

        given(authDao.findAccessTokenByTokenId(ACCESS_TOKEN_ID)).willReturn(Optional.of(accessToken));

        given(errorResponseResolver.getRedirectionPath(any())).willReturn(FAILED_LOGOUT_REDIRECTION);
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(LOGOUT_MAPPING, false, null, accessTokenCookie, userIdCookie);
        //THEN
        ArgumentCaptor<AuthContext> argumentCaptor = ArgumentCaptor.forClass(AuthContext.class);
        verify(errorResponseResolver).getRedirectionPath(argumentCaptor.capture());
        AuthContext authContext = argumentCaptor.getValue();
        verifyAuthContext(authContext);

        responseValidator.verifyRedirection(response, FAILED_LOGOUT_REDIRECTION);
    }

    private void verifyAuthContext(AuthContext authContext) {
        assertThat(authContext.getAccessStatus()).isEqualTo(AccessStatus.FORBIDDEN);
        assertThat(authContext.getRequestUri()).isEqualTo(LOGOUT_MAPPING);
        assertThat(authContext.getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(authContext.isRest()).isFalse();
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
