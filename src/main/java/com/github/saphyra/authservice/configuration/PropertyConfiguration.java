package com.github.saphyra.authservice.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import com.github.saphyra.authservice.domain.AllowedUri;
import lombok.Getter;

@Component
@Getter
public class PropertyConfiguration {
    public static final String LOGIN_ENDPOINT = "${com.github.saphyra.authservice.login.path:/login}";
    public static final String LOGOUT_ENDPOINT = "${com.github.saphyra.authservice.login.path:/logout}";

    @Value("${com.github.saphyra.authservice.filter.order:1}")
    private Integer filterOrder;

    @Value("${com.github.saphyra.authservice.cookie.access-token-id:cookie-access-token-id}")
    private String accessTokenIdCookie;

    @Value("${com.github.saphyra.authservice.cookie.user-id:cookie-user-id}")
    private String userIdCookie;

    @Value("${com.github.saphyra.authservice.access-token.expiration-seconds}")
    private long expirationSeconds;

    @Value("${com.github.saphyra.authservice.rest.request-type-header:}")
    private String requestTypeHeader;

    @Value("${com.github.saphyra.authservice.rest.rest-type-value:}")
    private String restTypeValue;

    @Value("${com.github.saphyra.authservice.login.successful-redirection:}")
    private String successfulLoginRedirection;

    @Value("${com.github.saphyra.authservice.logout.redirection:#{null}}")
    private String logoutRedirection;

    @Value("${com.github.saphyra.authservice.login.multiple-login-allowed}")
    private boolean multipleLoginAllowed;

    @Value(LOGIN_ENDPOINT)
    private String loginPath;

    @Value(LOGOUT_ENDPOINT)
    private String logoutPath;

    public List<AllowedUri> getDefaultAllowedUris() {
        return Arrays.asList(
            new AllowedUri(loginPath, HttpMethod.POST),
            new AllowedUri(logoutPath, HttpMethod.POST)
        );
    }
}
