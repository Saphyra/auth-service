package com.github.saphyra.authservice.integration.cases.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import javax.servlet.http.Cookie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.Credentials;
import com.github.saphyra.authservice.auth.domain.LoginRequest;
import com.github.saphyra.authservice.auth.domain.User;
import com.github.saphyra.authservice.integration.component.MockMvcWrapper;
import com.github.saphyra.authservice.integration.configuration.MvcConfiguration;
import com.github.saphyra.authservice.integration.domain.UrlEncodedLoginRequest;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {MvcConfiguration.class, SuccessfulLoginWithDisabledMultipleLogin.class})
public class SuccessfulLoginWithDisabledMultipleLogin {
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CREDENTIALS_PASSWORD = "credentials_password";
    private static final String USER_ID = "user_id";

    @Autowired
    private MockMvcWrapper mockMvcWrapper;

    @Autowired
    private AuthDao authDao;

    @Autowired
    private PropertyConfiguration propertyConfiguration;

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
    }

    @Test
    public void loginByRest_notRememberMe() throws Exception {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, false);
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/login", true, loginRequest);
        //THEN
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(authDao).deleteAccessTokenByUserId(USER_ID);

        ArgumentCaptor<AccessToken> saveAccessTokenArgumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).saveAccessToken(saveAccessTokenArgumentCaptor.capture());

        AccessToken accessToken = saveAccessTokenArgumentCaptor.getValue();
        assertThat(accessToken.getUserId()).isEqualTo(USER_ID);
        assertThat(accessToken.isPersistent()).isFalse();

        ArgumentCaptor<AccessToken> successfulLoginCallbackArgumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).successfulLoginCallback(successfulLoginCallbackArgumentCaptor.capture());
        assertThat(saveAccessTokenArgumentCaptor.getValue()).isEqualTo(successfulLoginCallbackArgumentCaptor.getValue());

        verifyCookies(response, accessToken.getAccessTokenId(), -1);
    }

    @Test
    public void loginByRest_rememberMe() throws Exception {
        //GIVEN
        LoginRequest loginRequest = new LoginRequest(USERNAME, PASSWORD, true);
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/login", true, loginRequest);
        //THEN
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(authDao).deleteAccessTokenByUserId(USER_ID);

        ArgumentCaptor<AccessToken> saveAccessTokenArgumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).saveAccessToken(saveAccessTokenArgumentCaptor.capture());

        AccessToken accessToken = saveAccessTokenArgumentCaptor.getValue();
        assertThat(accessToken.getUserId()).isEqualTo(USER_ID);
        assertThat(accessToken.isPersistent()).isTrue();

        ArgumentCaptor<AccessToken> successfulLoginCallbackArgumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).successfulLoginCallback(successfulLoginCallbackArgumentCaptor.capture());
        assertThat(saveAccessTokenArgumentCaptor.getValue()).isEqualTo(successfulLoginCallbackArgumentCaptor.getValue());

        verifyCookies(response, accessToken.getAccessTokenId(), Integer.MAX_VALUE);
    }

    @Test
    public void loginByForm_notRememberMe() throws Exception {
        //GIVEN
        UrlEncodedLoginRequest loginRequest = new UrlEncodedLoginRequest(new LoginRequest(USERNAME, PASSWORD, false));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/login", false, loginRequest);
        //THEN
        assertThat(response.getStatus()).isEqualTo(302);
        verify(authDao).deleteAccessTokenByUserId(USER_ID);

        ArgumentCaptor<AccessToken> saveAccessTokenArgumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).saveAccessToken(saveAccessTokenArgumentCaptor.capture());

        AccessToken accessToken = saveAccessTokenArgumentCaptor.getValue();
        assertThat(accessToken.getUserId()).isEqualTo(USER_ID);
        assertThat(accessToken.isPersistent()).isFalse();

        ArgumentCaptor<AccessToken> successfulLoginCallbackArgumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).successfulLoginCallback(successfulLoginCallbackArgumentCaptor.capture());
        assertThat(saveAccessTokenArgumentCaptor.getValue()).isEqualTo(successfulLoginCallbackArgumentCaptor.getValue());

        verifyCookies(response, accessToken.getAccessTokenId(), -1);
    }

    @Test
    public void loginByForm_rememberMe() throws Exception {
        //GIVEN
        UrlEncodedLoginRequest loginRequest = new UrlEncodedLoginRequest(new LoginRequest(USERNAME, PASSWORD, true));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest("/login", false, loginRequest);
        //THEN
        assertThat(response.getStatus()).isEqualTo(302);
        verify(authDao).deleteAccessTokenByUserId(USER_ID);

        ArgumentCaptor<AccessToken> saveAccessTokenArgumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).saveAccessToken(saveAccessTokenArgumentCaptor.capture());

        AccessToken accessToken = saveAccessTokenArgumentCaptor.getValue();
        assertThat(accessToken.getUserId()).isEqualTo(USER_ID);
        assertThat(accessToken.isPersistent()).isTrue();

        ArgumentCaptor<AccessToken> successfulLoginCallbackArgumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).successfulLoginCallback(successfulLoginCallbackArgumentCaptor.capture());
        assertThat(saveAccessTokenArgumentCaptor.getValue()).isEqualTo(successfulLoginCallbackArgumentCaptor.getValue());

        verifyCookies(response, accessToken.getAccessTokenId(), Integer.MAX_VALUE);
    }

    private void verifyCookies(MockHttpServletResponse response, String accessTokenId, int expectedMaxAge) {
        Cookie accessTokenIdCookie = response.getCookie(propertyConfiguration.getAccessTokenIdCookie());
        assertThat(accessTokenIdCookie).isNotNull();
        assertThat(accessTokenIdCookie.getValue()).isEqualTo(accessTokenId);
        assertThat(accessTokenIdCookie.getMaxAge()).isEqualTo(expectedMaxAge);

        Cookie userIdCookie = response.getCookie(propertyConfiguration.getUserIdCookie());
        assertThat(userIdCookie).isNotNull();
        assertThat(userIdCookie.getValue()).isEqualTo(USER_ID);
        assertThat(userIdCookie.getMaxAge()).isEqualTo(expectedMaxAge);
    }
}
