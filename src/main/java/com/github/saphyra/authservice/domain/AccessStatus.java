package com.github.saphyra.authservice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.servlet.http.HttpServletResponse;

@RequiredArgsConstructor
public enum AccessStatus {
    GRANTED(HttpServletResponse.SC_OK),
    UNAUTHORIZED(HttpServletResponse.SC_UNAUTHORIZED),
    FORBIDDEN(HttpServletResponse.SC_FORBIDDEN);

    @Getter
    private final int ResponseStatus;
}
