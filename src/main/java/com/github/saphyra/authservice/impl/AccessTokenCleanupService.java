package com.github.saphyra.authservice.impl;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.configuration.PropertyConfiguration;
import com.github.saphyra.util.OffsetDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
@EnableScheduling
class AccessTokenCleanupService {
    private final AuthDao authDao;
    private final OffsetDateTimeProvider offsetDateTimeProvider;
    private final PropertyConfiguration propertyConfiguration;

    @Scheduled(cron = "${com.github.saphyra.authservice.access-token.cleanup-interval-cron}")
    void deleteExpiredAccessTokens(){
        OffsetDateTime expiration = offsetDateTimeProvider.getCurrentDate().minusSeconds(propertyConfiguration.getExpirationSeconds());
        authDao.deleteExpiredAccessTokens(expiration);
    }
}
