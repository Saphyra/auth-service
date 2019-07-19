package com.github.saphyra.authservice.auth.impl;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.github.saphyra.authservice.auth.AuthDao;
import com.github.saphyra.authservice.auth.configuration.PropertyConfiguration;
import com.github.saphyra.authservice.auth.domain.AccessToken;
import com.github.saphyra.authservice.auth.domain.Credentials;
import com.github.saphyra.authservice.auth.domain.User;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import com.github.saphyra.util.IdGenerator;
import com.github.saphyra.util.OffsetDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
class LoginService {
    private final AuthDao authDao;
    private final IdGenerator idGenerator;
    private final OffsetDateTimeProvider offsetDateTimeProvider;
    private final PropertyConfiguration propertyConfiguration;

    AccessToken login(String userName, String password, Boolean rememberMe) {
        User user = authDao.findUserByUserName(userName)
            .orElseThrow(() -> new UnauthorizedException("User not found with userName " + userName));
        Credentials credentials = user.getCredentials();

        if (!authDao.authenticate(password, credentials.getPassword())) {
            throw new UnauthorizedException("Bad password.");
        }

        if (!propertyConfiguration.isMultipleLoginAllowed()) {
            log.debug("Multiple login is not allowed. Deleting accessTokens for user {}", user.getUserId());
            authDao.deleteAccessTokenByUserId(user.getUserId());
        }

        AccessToken accessToken = AccessToken.builder()
            .accessTokenId(idGenerator.generateRandomId())
            .userId(user.getUserId())
            .lastAccess(offsetDateTimeProvider.getCurrentDate())
            .isPersistent(Optional.ofNullable(rememberMe).orElse(false))
            .build();
        log.debug("AccessToken created: {}", accessToken);

        authDao.saveAccessToken(accessToken);
        authDao.successfulLoginCallback(accessToken);
        return accessToken;
    }
}
