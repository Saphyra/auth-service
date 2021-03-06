package com.github.saphyra.authservice.auth.impl;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.configuration.AuthProperties;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.Credentials;
import com.github.saphyra.authservice.auth.domain.User;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import com.github.saphyra.util.IdGenerator;
import com.github.saphyra.util.OffsetDateTimeProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LoginServiceTest {
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";
    private static final String PASSWORD_TOKEN = "password_token";
    private static final String USER_ID = "user_id";
    private static final String  ACCESS_TOKEN_ID = "access_token_id";
    private static final OffsetDateTime CURRENT_DATE = OffsetDateTime.now();
    @Mock
    private AuthDao authDao;

    @Mock
    private IdGenerator idGenerator;

    @Mock
    private OffsetDateTimeProvider offsetDateTimeProvider;

    @Mock
    private AuthProperties authProperties;

    @InjectMocks
    private LoginService underTest;

    @Before
    public void init() {
        User user = User.builder()
            .userId(USER_ID)
            .credentials(new Credentials(USER_NAME, PASSWORD_TOKEN))
            .build();
        when(authDao.findUserByUserName(USER_NAME)).thenReturn(Optional.of(user));
        when(authDao.authenticate(PASSWORD, PASSWORD_TOKEN)).thenReturn(true);
        when(authProperties.isMultipleLoginAllowed()).thenReturn(true);

        given(idGenerator.generateRandomId()).willReturn(ACCESS_TOKEN_ID);
        given(offsetDateTimeProvider.getCurrentDate()).willReturn(CURRENT_DATE);
    }

    @Test(expected = UnauthorizedException.class)
    public void testLoginShouldThrowExceptionWhenUserNotFound() {
        //GIVEN
        when(authDao.findUserByUserName(USER_NAME)).thenReturn(Optional.empty());
        //THEN
        underTest.login(USER_NAME, PASSWORD, false);
    }

    @Test(expected = UnauthorizedException.class)
    public void testLoginShouldThrowExceptionWhenBadPassword() {
        //GIVEN
        when(authDao.authenticate(PASSWORD, PASSWORD_TOKEN)).thenReturn(false);
        //WHEN
        underTest.login(USER_NAME, PASSWORD, false);
    }

    @Test
    public void testLoginShouldDeleteOthersWhenMultipleLoginNotAllowed(){
        //GIVEN
        when(authProperties.isMultipleLoginAllowed()).thenReturn(false);
        //WHEN
        underTest.login(USER_NAME, PASSWORD, false);
        //THEN
        verify(authDao).deleteAccessTokenByUserId(USER_ID);
    }

    @Test
    public void testLoginShouldCreateToken(){
        //WHEN
        underTest.login(USER_NAME, PASSWORD, null);
        //THEN
        ArgumentCaptor<AccessToken> argumentCaptor = ArgumentCaptor.forClass(AccessToken.class);
        verify(authDao).saveAccessToken(argumentCaptor.capture());
        assertEquals(ACCESS_TOKEN_ID, argumentCaptor.getValue().getAccessTokenId());
        assertEquals(USER_ID, argumentCaptor.getValue().getUserId());
        assertEquals(CURRENT_DATE, argumentCaptor.getValue().getLastAccess());
        assertFalse(argumentCaptor.getValue().isPersistent());
        verify(authDao).successfulLoginCallback(argumentCaptor.getValue());
    }
}