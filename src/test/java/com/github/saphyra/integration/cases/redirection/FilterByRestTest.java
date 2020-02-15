package com.github.saphyra.integration.cases.redirection;

import static com.github.saphyra.integration.component.TestController.ALLOWED_URI_MAPPING;
import static com.github.saphyra.integration.component.TestController.PROTECTED_URI_MAPPING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import javax.servlet.http.Cookie;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.saphyra.authservice.common.CommonAuthProperties;
import com.github.saphyra.authservice.redirection.RedirectionFilterSettings;
import com.github.saphyra.authservice.redirection.domain.ProtectedUri;
import com.github.saphyra.authservice.redirection.domain.RedirectionContext;
import com.github.saphyra.integration.component.MockMvcWrapper;
import com.github.saphyra.integration.component.ResponseValidator;
import com.github.saphyra.integration.component.TestController;
import com.github.saphyra.integration.configuration.RedirectionConfiguration;

@RunWith(SpringRunner.class)
@WebMvcTest
@AutoConfigureMockMvc(secure = false)
@ContextConfiguration(classes = {RedirectionConfiguration.class, FilterByRestTest.class})
public class FilterByRestTest {
    private static final String ACCESS_TOKEN_ID = "access_token_id";
    private static final String USER_ID = "user_id";

    @Autowired
    private MockMvcWrapper mockMvcWrapper;

    @Autowired
    private CommonAuthProperties commonAuthProperties;

    @Autowired
    private ResponseValidator responseValidator;

    @MockBean
    private RedirectionFilterSettings redirectionFilterSettings;

    @Captor
    private ArgumentCaptor<RedirectionContext> argumentCaptor;

    @Test
    public void allowedUri() throws Exception {
        //WHEN
        given(redirectionFilterSettings.getProtectedUri()).willReturn(new ProtectedUri(ALLOWED_URI_MAPPING, new HashSet<>(Arrays.asList(HttpMethod.GET))));
        MockHttpServletResponse response = mockMvcWrapper.postRequest(PROTECTED_URI_MAPPING, true, null);
        //THEN
        verifyOk(response);
    }

    @Test
    public void protectedUri_allowedMethod() throws Exception {
        //GIVEN
        given(redirectionFilterSettings.getProtectedUri()).willReturn(new ProtectedUri(PROTECTED_URI_MAPPING, new HashSet<>(Arrays.asList(HttpMethod.GET))));
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(PROTECTED_URI_MAPPING, true, null);
        //THEN
        verifyOk(response);
    }

    @Test
    public void protectedUriAndMethod_shouldNotRedirect() throws Exception {
        //GIVEN
        given(redirectionFilterSettings.getProtectedUri()).willReturn(new ProtectedUri(PROTECTED_URI_MAPPING, new HashSet<>(Arrays.asList(HttpMethod.POST))));
        given(redirectionFilterSettings.shouldRedirect(any())).willReturn(false);
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonAuthProperties.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonAuthProperties.getUserIdCookie(), USER_ID)
        );
        //THEN
        verify(redirectionFilterSettings).shouldRedirect(argumentCaptor.capture());

        verifyRedirectionContext(Optional.of(ACCESS_TOKEN_ID), Optional.of(USER_ID));
        verifyOk(response);
    }

    @Test
    public void protectedUriAndMethod_shouldRedirect() throws Exception {
        //GIVEN
        given(redirectionFilterSettings.getProtectedUri()).willReturn(new ProtectedUri(PROTECTED_URI_MAPPING, new HashSet<>(Arrays.asList(HttpMethod.POST))));
        given(redirectionFilterSettings.shouldRedirect(any())).willReturn(true);

        given(redirectionFilterSettings.getRedirectionPath(any())).willReturn(ALLOWED_URI_MAPPING);
        //WHEN
        MockHttpServletResponse response = mockMvcWrapper.postRequest(
            PROTECTED_URI_MAPPING,
            true,
            null,
            createCookie(commonAuthProperties.getAccessTokenIdCookie(), ACCESS_TOKEN_ID),
            createCookie(commonAuthProperties.getUserIdCookie(), USER_ID)
        );
        //THEN
        responseValidator.verifyRedirection(response, ALLOWED_URI_MAPPING);

        verify(redirectionFilterSettings).shouldRedirect(argumentCaptor.capture());

        verifyRedirectionContext(Optional.of(ACCESS_TOKEN_ID), Optional.of(USER_ID));
    }

    private void verifyOk(MockHttpServletResponse response) throws UnsupportedEncodingException {
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getContentAsString()).isEqualTo(PROTECTED_URI_MAPPING);
    }

    private void verifyRedirectionContext(Optional<String> accessTokenId, Optional<String> userId) {
        RedirectionContext authContext = argumentCaptor.getValue();
        assertThat(authContext.getRequestUri()).isEqualTo(TestController.PROTECTED_URI_MAPPING);
        assertThat(authContext.getRequestMethod()).isEqualTo(HttpMethod.POST);
        assertThat(authContext.isRest()).isTrue();
        assertThat(authContext.getAccessTokenId()).isEqualTo(accessTokenId);
        assertThat(authContext.getUserId()).isEqualTo(userId);
    }

    private Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        return cookie;
    }
}
