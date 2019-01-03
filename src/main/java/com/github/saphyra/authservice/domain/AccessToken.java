package com.github.saphyra.authservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessToken {
    private String accessTokenId;
    private String userId;
    private boolean isPersistent;
    private OffsetDateTime lastAccess;
}
