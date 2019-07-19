package com.github.saphyra.authservice.auth.domain;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessToken {
    @NonNull
    private String accessTokenId;

    @NonNull
    private String userId;
    private boolean isPersistent;

    @NonNull
    private OffsetDateTime lastAccess;
}
