package com.github.saphyra.authservice.auth.impl;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenCacheTest {
    private static final String ACCESS_TOKEN_ID = "access-token-id";
    @Mock
    private AuthDao authDao;

    @InjectMocks
    private AccessTokenCache underTest;

    @Mock
    private AccessToken accessToken;

    @Test
    public void get() {
        //GIVEN
        given(authDao.findAccessTokenByTokenId(ACCESS_TOKEN_ID)).willReturn(Optional.of(accessToken));
        //WHEN
        Optional<AccessToken> result = underTest.get(ACCESS_TOKEN_ID);
        underTest.get(ACCESS_TOKEN_ID);
        //THEN
        verify(authDao, times(1)).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        assertThat(result).contains(accessToken);
    }
}