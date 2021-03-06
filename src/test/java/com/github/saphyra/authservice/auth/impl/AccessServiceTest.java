package com.github.saphyra.authservice.auth.impl;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.UriConfiguration;
import com.github.saphyra.authservice.auth.configuration.AuthProperties;
import com.github.saphyra.authservice.auth.domain.AccessStatus;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.AllowedUri;
import com.github.saphyra.authservice.auth.domain.RoleSetting;
import com.github.saphyra.authservice.auth.domain.User;
import com.github.saphyra.util.OffsetDateTimeProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessServiceTest {
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String REQUEST_URI = "request/uri";
    private static final String USER_ID = "user_id";
    private static final OffsetDateTime LAST_ACCESS = OffsetDateTime.now();
    private static final String FAKE_USER_ID = "fake_user_id";
    private static final OffsetDateTime CURRENT_DATE = OffsetDateTime.now();
    private static final Long EXPIRATION_SECONDS = 5L;
    private static final String USER_ROLE = "user_role";

    @Mock
    private AccessTokenCache accessTokenCache;

    @Mock
    private AntPathMatcher antPathMatcher;

    @Mock
    private AuthDao authDao;

    @Mock
    private OffsetDateTimeProvider offsetDateTimeProvider;

    @Mock
    private UriConfiguration uriConfiguration;

    @Mock
    private AuthProperties authProperties;

    private AccessToken accessToken;

    private User user;

    @InjectMocks
    private AccessService underTest;

    @Before
    public void init() {
        accessToken = AccessToken.builder()
            .accessTokenId(ACCESS_TOKEN_ID)
            .userId(USER_ID)
            .lastAccess(LAST_ACCESS)
            .isPersistent(true)
            .build();

        when(accessTokenCache.get(ACCESS_TOKEN_ID)).thenReturn(Optional.of(accessToken));
        when(offsetDateTimeProvider.getCurrentDate()).thenReturn(CURRENT_DATE);
        when(authProperties.getExpirationSeconds()).thenReturn(EXPIRATION_SECONDS);

        user = User.builder()
            .userId(USER_ID)
            .roles(new HashSet<>(Arrays.asList(USER_ROLE)))
            .build();

        when(authDao.findUserById(USER_ID)).thenReturn(Optional.of(user));

        when(antPathMatcher.match(REQUEST_URI, REQUEST_URI)).thenReturn(true);
    }

    @Test
    public void testCanAccessShouldReturnUnauthorizedWhenAccessTokenNotFound() {
        //GIVEN
        when(accessTokenCache.get(ACCESS_TOKEN_ID)).thenReturn(Optional.empty());
        //WHEN
        assertEquals(AccessStatus.ACCESS_TOKEN_NOT_FOUND, underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnUnauthorizedWhenBadUserId() {
        //WHEN
        assertEquals(AccessStatus.INVALID_USER_ID, underTest.canAccess(REQUEST_URI, HttpMethod.POST, FAKE_USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnUnauthorizedWhenTokenExpired() {
        //GIVEN
        accessToken.setPersistent(false);
        accessToken.setLastAccess(OffsetDateTime.now().minusDays(2));
        //WHEN
        assertEquals(AccessStatus.ACCESS_TOKEN_EXPIRED, underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnUnauthorizedWhenUserNotFound() {
        //GIVEN
        when(authDao.findUserById(USER_ID)).thenReturn(Optional.empty());
        //WHEN
        assertEquals(AccessStatus.USER_NOT_FOUND, underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnGrantedWhenUriNotProtected() {
        //GIVEN
        when(uriConfiguration.getRoleSettings()).thenReturn(new HashSet<>());
        //WHEN
        assertEquals(AccessStatus.GRANTED, underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnForbiddenWhenUserHasNoRole() {
        //GIVEN
        user.setRoles(new HashSet<>());
        Set<RoleSetting> protectedURIs = new HashSet<>();
        protectedURIs.add(
            RoleSetting.builder()
                .uri(REQUEST_URI)
                .addProtectedMethod(HttpMethod.POST)
                .addRole(USER_ROLE)
                .build()
        );
        when(uriConfiguration.getRoleSettings()).thenReturn(protectedURIs);
        //WHEN
        AccessStatus result = underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID);
        //THEN
        assertEquals(AccessStatus.FORBIDDEN, result);
        verify(authDao).saveAccessToken(accessToken);
        assertEquals(CURRENT_DATE, accessToken.getLastAccess());
    }

    @Test
    public void testCanAccessShouldReturnGrantedWhenDifferentMethod() {
        //GIVEN
        Set<RoleSetting> protectedURIs = new HashSet<>();
        protectedURIs.add(
            RoleSetting.builder()
                .uri(REQUEST_URI)
                .addProtectedMethod(HttpMethod.GET)
                .addRole(USER_ROLE)
                .build()
        );
        when(uriConfiguration.getRoleSettings()).thenReturn(protectedURIs);
        //WHEN
        AccessStatus result = underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID);
        //THEN
        assertEquals(AccessStatus.GRANTED, result);
        verify(authDao).saveAccessToken(accessToken);
        assertEquals(CURRENT_DATE, accessToken.getLastAccess());
    }

    @Test
    public void testCanAccessShouldReturnGrantedWhenUserHasRole() {
        //GIVEN
        user.setRoles(new HashSet<>(Arrays.asList(USER_ROLE)));
        Set<RoleSetting> protectedURIs = new HashSet<>();
        protectedURIs.add(
            RoleSetting.builder()
                .uri(REQUEST_URI)
                .addProtectedMethod(HttpMethod.POST)
                .addRole(USER_ROLE)
                .build()
        );
        when(uriConfiguration.getRoleSettings()).thenReturn(protectedURIs);
        //WHEN
        AccessStatus result = underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID);
        //THEN
        assertEquals(AccessStatus.GRANTED, result);
        verify(authDao).saveAccessToken(accessToken);
        assertEquals(CURRENT_DATE, accessToken.getLastAccess());
    }

    @Test
    public void canAccess_doesNotUpdateLastAccess(){
        //GIVEN
        user.setRoles(new HashSet<>(Arrays.asList(USER_ROLE)));
        Set<RoleSetting> protectedURIs = new HashSet<>();
        protectedURIs.add(
            RoleSetting.builder()
                .uri(REQUEST_URI)
                .addProtectedMethod(HttpMethod.POST)
                .addRole(USER_ROLE)
                .build()
        );
        when(uriConfiguration.getRoleSettings()).thenReturn(protectedURIs);
        when(uriConfiguration.getNonSessionExtendingUris()).thenReturn(Arrays.asList(new AllowedUri(REQUEST_URI, HttpMethod.POST)));
        //WHEN
        AccessStatus result = underTest.canAccess(REQUEST_URI, HttpMethod.POST, USER_ID, ACCESS_TOKEN_ID);
        //THEN
        assertEquals(AccessStatus.GRANTED, result);
        verify(authDao).findUserById(USER_ID);
        verifyNoMoreInteractions(authDao);
    }
}