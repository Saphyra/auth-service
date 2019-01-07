package com.github.saphyra.authservice.service;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.User;
import com.github.saphyra.util.OffsetDateTimeProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.util.AntPathMatcher;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AccessServiceTest {
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String REQUEST_URI = "request/uri";
    private static final String USER_ID = "user_id";
    private static final OffsetDateTime LAST_ACCESS = OffsetDateTime.now();
    private static final String FAKE_USER_ID = "fake_user_id";
    private static final OffsetDateTime CURRENT_DATE = OffsetDateTime.now();
    private static final Long EXPIRATION_MINUTES = 5L;
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
    private PropertySource propertySource;

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
        when(propertySource.getTokenExpirationMinutes()).thenReturn(EXPIRATION_MINUTES);

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
        assertEquals(AccessStatus.UNAUTHORIZED, underTest.canAccess(REQUEST_URI, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnUnauthorizedWhenBadUserId() {
        //WHEN
        assertEquals(AccessStatus.UNAUTHORIZED, underTest.canAccess(REQUEST_URI, FAKE_USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnUnauthorizedWhenTokenExpired() {
        //GIVEN
        accessToken.setPersistent(false);
        accessToken.setLastAccess(OffsetDateTime.now().minusDays(2));
        //WHEN
        assertEquals(AccessStatus.UNAUTHORIZED, underTest.canAccess(REQUEST_URI, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnUnauthorizedWhenUserNotFound() {
        //GIVEN
        when(authDao.findUserById(USER_ID)).thenReturn(Optional.empty());
        //WHEN
        assertEquals(AccessStatus.UNAUTHORIZED, underTest.canAccess(REQUEST_URI, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnGrantedWhenUriNotProtected(){
        //GIVEN
        when(propertySource.getRoleSettings()).thenReturn(new HashMap<>());
        //WHEN
        assertEquals(AccessStatus.GRANTED, underTest.canAccess(REQUEST_URI, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnForbiddenWhenUserHasNoRole(){
        //GIVEN
        user.setRoles(new HashSet<>());
        Map<String, Set<String>> protectedURIs = new HashMap<>();
        protectedURIs.put(REQUEST_URI, new HashSet<>(Arrays.asList(USER_ROLE)));
        when(propertySource.getRoleSettings()).thenReturn(protectedURIs);
        //WHEN
        assertEquals(AccessStatus.FORBIDDEN, underTest.canAccess(REQUEST_URI, USER_ID, ACCESS_TOKEN_ID));
    }

    @Test
    public void testCanAccessShouldReturnGrantedWhenUserHasRole(){
        //GIVEN
        user.setRoles(new HashSet<>(Arrays.asList(USER_ROLE)));
        Map<String, Set<String>> protectedURIs = new HashMap<>();
        protectedURIs.put(REQUEST_URI, new HashSet<>(Arrays.asList(USER_ROLE)));
        when(propertySource.getRoleSettings()).thenReturn(protectedURIs);
        //WHEN
        assertEquals(AccessStatus.GRANTED, underTest.canAccess(REQUEST_URI, USER_ID, ACCESS_TOKEN_ID));
    }
}