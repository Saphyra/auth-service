package com.github.saphyra.authservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccessToken {
    private String accessTokenId;
    private String userId;
    private OffsetDateTime lastAccess;
}
