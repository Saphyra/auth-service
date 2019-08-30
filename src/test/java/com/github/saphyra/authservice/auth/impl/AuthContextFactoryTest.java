package com.github.saphyra.authservice.auth.impl;

import com.github.saphyra.authservice.auth.domain.AccessStatus;
import com.github.saphyra.authservice.auth.domain.AuthContext;
import com.github.saphyra.authservice.common.CommonPropertyConfiguration;
import com.github.saphyra.authservice.common.RequestHelper;
import com.github.saphyra.util.CookieUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(MockitoJUnitRunner.class)
public class AuthContextFactoryTest {
    private static final String REQUEST_URI = "request_uri";
    private static final String COOKIE_ACCESS_TOKEN_ID = "cookie_access_token_id";
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String COOKIE_USER_ID = "cookie_user_id";
    private static final String USER_ID = "user_id";

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private CommonPropertyConfiguration commonPropertyConfiguration;

    @Mock
    private RequestHelper requestHelper;

    @InjectMocks
    private AuthContextFactory underTest;

    @Mock
    private HttpServletRequest request;

    @Test
    public void create() {
        //GIVEN
        given(commonPropertyConfiguration.getAccessTokenIdCookie()).willReturn(COOKIE_ACCESS_TOKEN_ID);
        given(commonPropertyConfiguration.getUserIdCookie()).willReturn(COOKIE_USER_ID);

        given(request.getRequestURI()).willReturn(REQUEST_URI);
        given(requestHelper.getMethod(request)).willReturn(HttpMethod.POST);
        given(requestHelper.isRestCall(request)).willReturn(true);
        given(cookieUtil.getCookie(request, COOKIE_ACCESS_TOKEN_ID)).willReturn(Optional.of(ACCESS_TOKEN_ID));
        given(cookieUtil.getCookie(request, COOKIE_USER_ID)).willReturn(Optional.of(USER_ID));
        //WHEN
        AuthContext result = underTest.create(request, AccessStatus.LOGIN_FAILED);
        //THEN
        assertThat(result.getRequestUri()).isEqualTo(REQUEST_URI);
        assertThat(result.getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(result.isRest()).isTrue();
        assertThat(result.getAccessTokenId()).contains(ACCESS_TOKEN_ID);
        assertThat(result.getUserId()).contains(USER_ID);
        assertThat(result.getAccessStatus()).isEqualTo(AccessStatus.LOGIN_FAILED);
        assertThat(result.getRequest()).isEqualTo(request);
    }
}