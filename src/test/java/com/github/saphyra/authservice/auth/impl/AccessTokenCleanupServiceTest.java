package com.github.saphyra.authservice.auth.impl;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.configuration.PropertyConfiguration;
import com.github.saphyra.util.OffsetDateTimeProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessTokenCleanupServiceTest {
    @Mock
    private AuthDao authDao;

    @Mock
    private OffsetDateTimeProvider offsetDateTimeProvider;

    @Mock
    private PropertyConfiguration propertyConfiguration;

    @InjectMocks
    private AccessTokenCleanupService underTest;

    @Test
    public void testCleanup(){
        //GIVEN
        when(offsetDateTimeProvider.getCurrentDate()).thenReturn(OffsetDateTime.now());
        when(propertyConfiguration.getExpirationSeconds()).thenReturn(1L);
        //WHEN
        underTest.deleteExpiredAccessTokens();
        //THEN
        verify(authDao).deleteExpiredAccessTokens(any(OffsetDateTime.class));
    }

}