package com.github.saphyra.authservice.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AccessToken {
    private String accessTokenId;
    private String userId;
    private LocalDateTime lastAccess;
}
