package com.github.saphyra.authservice.auth.impl;

import java.time.OffsetDateTime;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.configuration.AuthProperties;
import com.github.saphyra.util.OffsetDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableScheduling
class AccessTokenCleanupService {
    private final AuthDao authDao;
    private final OffsetDateTimeProvider offsetDateTimeProvider;
    private final AuthProperties authProperties;

    @Scheduled(cron = "${com.github.saphyra.authservice.auth.access-token.cleanup-interval-cron}")
    void deleteExpiredAccessTokens(){
        OffsetDateTime expiration = offsetDateTimeProvider.getCurrentDate().minusSeconds(authProperties.getExpirationSeconds());
        authDao.deleteExpiredAccessTokens(expiration);
    }
}
