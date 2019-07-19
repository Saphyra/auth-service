package com.github.saphyra.integration.cases.auth.filter;

import static com.github.saphyra.integration.component.TestController.ALLOWED_URI_MAPPING;
import static com.github.saphyra.integration.component.TestController.PROTECTED_URI_MAPPING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.ErrorResponseResolver;
import com.github.saphyra.authservice.auth.UriConfiguration;
import com.github.saphyra.authservice.auth.domain.AccessStatus;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.AllowedUri;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.auth.domain.RestErrorResponse;
import com.github.saphyra.authservice.auth.domain.RoleSetting;
import com.github.saphyra.authservice.auth.domain.User;
import com.github.saphyra.authservice.common.CommonPropertyConfiguration;
import com.github.saphyra.cache.AbstractCache;
import com.github.saphyra.integration.component.MockMvcWrapper;
import com.github.saphyra.integration.component.TestController;
import com.github.saphyra.integration.configuration.AuthConfiguration;
import com.github.saphyra.integration.domain.TestErrorResponse;
import com.github.saphyra.util.ObjectMapperWrapper;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {AuthConfiguration.class, RestFilterTest.class})
public class RestFilterTest {
    private static final String ERROR_MESSAGE = "error_message";
    private static final TestErrorResponse ERROR_RESPONSE = new TestErrorResponse(ERROR_MESSAGE);
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String USER_ID = "user_id";
    private static final String FAKE_USER_ID = "fake_user_id";
    private static final OffsetDateTime LAST_ACCESS = OffsetDateTime.now();
    private static final String ROLE_1 = "role_1";
    private static final String ROLE_2 = "role_2";

    @Autowired
    private MockMvcWrapper mockMvcWrapper;

    @Autowired
    private ErrorResponseResolver errorResponseResolver;

    @Autowired
    private ObjectMapperWrapper objectMapperWrapper;

    @Autowired
    private CommonPropertyConfiguration commonPropertyConfiguration;

    @Autowired
    private AuthDao authDao;

    @Autowired
    private AbstractCache<String, AccessToken> accessTokenCache;

    @Autowired
    private UriConfiguration uriConfiguration;

    @Captor
    private ArgumentCaptor<AuthContext> argumentCaptor;

    @Mock
    private AccessToken accessToken;

    @Mock
    private User user;

    @Before
    public void setUp() {
        given(authDao.findAccessTokenByTokenId(ACCESS_TOKEN_ID)).willReturn(Optional.of(accessToken));
        given(authDao.findUserById(USER_ID)).willReturn(Optional.of(user));

        given(accessToken.getAccessTokenId()).willReturn(ACCESS_TOKEN_ID);
        given(accessToken.getUserId()).willReturn(USER_ID);
        given(accessToken.getLastAccess()).willReturn(LAST_ACCESS);
        given(accessToken.isPersistent()).willReturn(false);
    }

    @After
    public void cleanUp() {
        accessTokenCache.invalidate(ACCESS_TOKEN_ID);
    }

    @Test
    public void accessAllowedUri() throws Exception {
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(ALLOWED_URI_MAPPING, true, null);
        //THEN
        verifyOk(response, ALLOWED_URI_MAPPING);
    }

    @Test
    public void accessProtectedUriWithAllowedPath() throws Exception {
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.putRequest(PROTECTED_URI_MAPPING, true, null);
        //THEN
        verifyOk(response, PROTECTED_URI_MAPPING);
    }

    @Test
    public void accessProtectedUriWithoutLogin() throws Exception {
        //GIVEN
        givenUnauthorizedResponse();
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(PROTECTED_URI_MAPPING, true, null);
        //THEN
        verifyUnauthorized(response);
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        verifyAuthContext(Optional.empty(), Optional.empty(), AccessStatus.UNAUTHORIZED);
    }

    @Test
    public void accessProtectedUriWrongAccessTokenId() throws Exception {
        //GIVEN
        givenUnauthorizedResponse();
        given(authDao.findAccessTokenByTokenId(ACCESS_TOKEN_ID)).willReturn(Optional.empty());
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verifyUnauthorized(response);
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        verifyAuthContextWithCookies(AccessStatus.UNAUTHORIZED);
    }

    @Test
    public void accessProtectedUriWrongUserId() throws Exception {
        //GIVEN
        givenUnauthorizedResponse();
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), FAKE_USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verifyUnauthorized(response);
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        verifyAuthContext(Optional.of(ACCESS_TOKEN_ID), Optional.of(FAKE_USER_ID), AccessStatus.UNAUTHORIZED);
    }

    @Test
    public void accessProtectedUriExpiredAccessToken() throws Exception {
        //GIVEN
        givenUnauthorizedResponse();
        given(accessToken.getLastAccess()).willReturn(OffsetDateTime.now().minusYears(1));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verifyUnauthorized(response);
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        verifyAuthContextWithCookies(AccessStatus.UNAUTHORIZED);
    }

    @Test
    public void accessProtectedUserNotFound() throws Exception {
        //GIVEN
        givenUnauthorizedResponse();
        given(authDao.findUserById(USER_ID)).willReturn(Optional.empty());
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);
        verifyUnauthorized(response);
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        verifyAuthContextWithCookies(AccessStatus.UNAUTHORIZED);
    }

    @Test
    public void accessExtendingUri() throws Exception {
        //GIVEN
        given(uriConfiguration.getRoleSettings()).willReturn(createRoleSettings());
        given(uriConfiguration.getNonSessionExtendingUris()).willReturn(Collections.emptyList());
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).saveAccessToken(accessToken);
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);
        verifyOk(response, PROTECTED_URI_MAPPING);
    }

    @Test
    public void accessNonExtendingUri() throws Exception {
        //GIVEN
        given(uriConfiguration.getRoleSettings()).willReturn(createRoleSettings());
        given(uriConfiguration.getNonSessionExtendingUris()).willReturn(Arrays.asList(new AllowedUri(PROTECTED_URI_MAPPING, HttpMethod.POST)));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao, times(0)).saveAccessToken(accessToken);
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);
        verifyOk(response, PROTECTED_URI_MAPPING);
    }

    @Test
    public void persistentAccessToken() throws Exception {
        //GIVEN
        given(uriConfiguration.getRoleSettings()).willReturn(createRoleSettings());
        given(accessToken.isPersistent()).willReturn(true);
        given(accessToken.getLastAccess()).willReturn(OffsetDateTime.now().minusYears(1));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);
        verifyOk(response, PROTECTED_URI_MAPPING);
    }

    @Test
    public void accessProtected_notProtected() throws Exception {
        //GIVEN
        given(uriConfiguration.getRoleSettings()).willReturn(createRoleSettings());
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);
        verifyOk(response, PROTECTED_URI_MAPPING);
    }

    @Test
    public void accessProtected_protectedUriAllowedMethod() throws Exception {
        //GIVEN
        RoleSetting roleSetting = RoleSetting.builder().uri(PROTECTED_URI_MAPPING).addRole(ROLE_1).addProtectedMethod(HttpMethod.GET).build();
        given(uriConfiguration.getRoleSettings()).willReturn(createRoleSettings(roleSetting));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);
        verifyOk(response, PROTECTED_URI_MAPPING);
    }

    @Test
    public void accessProtected_protectedUriProtectedMethod_noRole() throws Exception {
        //GIVEN
        RoleSetting roleSetting = RoleSetting.builder().uri(PROTECTED_URI_MAPPING).addRole(ROLE_1).addProtectedMethod(HttpMethod.POST).build();
        given(uriConfiguration.getRoleSettings()).willReturn(createRoleSettings(roleSetting));
        given(user.getRoles()).willReturn(new HashSet<>(Arrays.asList(ROLE_2)));
        givenForbiddenResponse();
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);

        verifyForbidden(response);
        verify(errorResponseResolver).getRestErrorResponse(argumentCaptor.capture());
        verifyAuthContextWithCookies(AccessStatus.FORBIDDEN);
    }

    @Test
    public void accessProtected_protectedUriProtectedMethod_hasRole() throws Exception {
        //GIVEN
        RoleSetting roleSetting = RoleSetting.builder().uri(PROTECTED_URI_MAPPING).addRole(ROLE_1).addRole(ROLE_2).addProtectedMethod(HttpMethod.POST).build();
        given(uriConfiguration.getRoleSettings()).willReturn(createRoleSettings(roleSetting));
        given(user.getRoles()).willReturn(new HashSet<>(Arrays.asList(ROLE_2)));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonPropertyConfiguration.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonPropertyConfiguration.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);

        verify(authDao).findAccessTokenByTokenId(ACCESS_TOKEN_ID);
        verify(authDao).findUserById(USER_ID);
        verifyOk(response, PROTECTED_URI_MAPPING);
    }

    private Set<RoleSetting> createRoleSettings(RoleSetting... roleSettings) {
        return new HashSet<>(Arrays.asList(roleSettings));
    }

    private void verifyAuthContextWithCookies(AccessStatus accessStatus) {
        verifyAuthContext(Optional.of(ACCESS_TOKEN_ID), Optional.of(USER_ID), accessStatus);
    }

    private void verifyAuthContext(Optional<String> accessTokenId, Optional<String> userId, AccessStatus accessStatus) {
        AuthContext authContext = argumentCaptor.getValue();
        assertThat(authContext.getRequestUri()).isEqualTo(TestController.PROTECTED_URI_MAPPING);
        assertThat(authContext.getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(authContext.isRest()).isTrue();
        assertThat(authContext.getAccessTokenId()).isEqualTo(accessTokenId);
        assertThat(authContext.getUserId()).isEqualTo(userId);
        assertThat(authContext.getAccessStatus()).isEqualTo(accessStatus);
    }

    private void givenUnauthorizedResponse() {
        RestErrorResponse restErrorResponse = new RestErrorResponse(HttpStatus.UNAUTHORIZED, ERROR_RESPONSE);
        given(errorResponseResolver.getRestErrorResponse(any())).willReturn(restErrorResponse);
    }

    private void givenForbiddenResponse() {
        RestErrorResponse restErrorResponse = new RestErrorResponse(HttpStatus.FORBIDDEN, ERROR_RESPONSE);
        given(errorResponseResolver.getRestErrorResponse(any())).willReturn(restErrorResponse);
    }

    private void verifyUnauthorized(MockHttpServletResponse response) throws UnsupportedEncodingException {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(objectMapperWrapper.readValue(response.getContentAsString(), TestErrorResponse.class).getErrorMessage()).isEqualTo(ERROR_MESSAGE);
    }

    private void verifyForbidden(MockHttpServletResponse response) throws UnsupportedEncodingException {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(objectMapperWrapper.readValue(response.getContentAsString(), TestErrorResponse.class).getErrorMessage()).isEqualTo(ERROR_MESSAGE);
    }

    private void verifyOk(MockHttpServletResponse response, String expectedResponseBody) throws UnsupportedEncodingException {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(expectedResponseBody);
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        return cookie;
    }
}
