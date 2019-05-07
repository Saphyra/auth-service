package com.github.saphyra.authservice.impl;

import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AccessToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceImplTest {
    private static final String REQUEST_URI = "request_uri";
    private static final String USER_ID = "user_id";
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";
    @Mock
    private AccessService accessService;

    @Mock
    private LoginService loginService;

    @Mock
    private LogoutService logoutService;

    @InjectMocks
    private AuthServiceImpl underTest;

    @Test
    public void testCanAccessShouldCallServiceAndReturn(){
        //GIVEN
        when(accessService.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID)).thenReturn(AccessStatus.GRANTED);
        //WHEN
        assertEquals(AccessStatus.GRANTED, underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID));
        //THEN
        verify(accessService).canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID);
    }

    @Test
    public void testLogin(){
        //GIVEN
        AccessToken accessToken = AccessToken.builder().accessTokenId(ACCESS_TOKEN_ID).build();
        when(loginService.login(USER_NAME, PASSWORD, false)).thenReturn(accessToken);
        //WHEN
        assertEquals(accessToken, underTest.login(USER_NAME, PASSWORD, false));
        //THEN
        verify(loginService).login(USER_NAME, PASSWORD, false);
    }

    @Test
    public void testLogout(){
        //WHEN
        underTest.logout(USER_ID, ACCESS_TOKEN_ID);
        //THEN
        verify(logoutService).logout(USER_ID, ACCESS_TOKEN_ID);
    }
}