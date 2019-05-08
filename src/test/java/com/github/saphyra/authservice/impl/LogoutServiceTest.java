package com.github.saphyra.authservice.impl;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.impl.AccessTokenCache;
import com.github.saphyra.authservice.impl.LogoutService;
import com.github.saphyra.exceptionhandling.exception.ForbiddenException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogoutServiceTest {
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String USER_ID = "user_id";
    private static final String FAKE_USER_ID = "fake_user_id";
    @Mock
    private AccessTokenCache accessTokenCache;

    @Mock
    private  AuthDao authDao;

    @InjectMocks
    private LogoutService underTest;

    @Test(expected = ForbiddenException.class)
    public void testLogoutShouldThrowExceptionWhenForbidden(){
        //GIVEN
        AccessToken accessToken = AccessToken.builder()
            .accessTokenId(ACCESS_TOKEN_ID)
            .userId(USER_ID)
            .build();

        when(accessTokenCache.get(ACCESS_TOKEN_ID)).thenReturn(Optional.of(accessToken));
        //WHEN
        underTest.logout(FAKE_USER_ID, ACCESS_TOKEN_ID);
    }

    @Test
    public void testLogoutShouldDeleteTokenAndInvalidate(){
        //GIVEN
        AccessToken accessToken = AccessToken.builder()
            .accessTokenId(ACCESS_TOKEN_ID)
            .userId(USER_ID)
            .build();

        when(accessTokenCache.get(ACCESS_TOKEN_ID)).thenReturn(Optional.of(accessToken));
        //WHEN
        underTest.logout(USER_ID, ACCESS_TOKEN_ID);
        //THEN
        verify(accessTokenCache).invalidate(ACCESS_TOKEN_ID);
        verify(authDao).deleteAccessToken(accessToken);
        verify(authDao).successfulLogoutCallback(accessToken);
    }

}