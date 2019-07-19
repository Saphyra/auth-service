package com.github.saphyra.integration.domain;

import com.github.saphyra.authservice.auth.domain.LoginRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UrlEncodedLoginRequest {
    private final LoginRequest loginRequest;

    @Override
    public String toString() {
        return String.format(
            "userName=%s&password=%s&rememberMe=%s",
            loginRequest.getUserName(),
            loginRequest.getPassword(),
            loginRequest.getRememberMe()
        );
    }
}
