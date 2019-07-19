package com.github.saphyra.authservice.auth.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AccessStatus {
    GRANTED,
    UNAUTHORIZED,
    FORBIDDEN;
}
