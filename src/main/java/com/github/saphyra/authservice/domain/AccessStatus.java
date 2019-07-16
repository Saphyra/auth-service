package com.github.saphyra.authservice.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum AccessStatus {
    GRANTED,
    UNAUTHORIZED,
    FORBIDDEN;
}
