package com.github.saphyra.authservice.auth.configuration;

import com.github.saphyra.authservice.auth.domain.AllowedUri;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Getter
public class AuthPropertyConfiguration {
    public static final String LOGIN_ENDPOINT = "${com.github.saphyra.authservice.auth.login.path:/login}";
    public static final String LOGOUT_ENDPOINT = "${com.github.saphyra.authservice.auth.logout.path:/logout}";

    @Value("${com.github.saphyra.authservice.auth.filter.order:1}")
    private Integer filterOrder;

    @Value("${com.github.saphyra.authservice.auth.access-token.expiration-seconds}")
    private long expirationSeconds;

    @Value("${com.github.saphyra.authservice.auth.login.successful-redirection:}")
    private String successfulLoginRedirection;

    @Value("${com.github.saphyra.authservice.auth.logout.redirection:#{null}}")
    private String logoutRedirection;

    @Value("${com.github.saphyra.authservice.auth.login.multiple-login-allowed}")
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
