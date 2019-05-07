package com.github.saphyra.authservice.impl;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.Credentials;
import com.github.saphyra.authservice.domain.User;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import com.github.saphyra.util.IdGenerator;
import com.github.saphyra.util.OffsetDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
class LoginService {
    private final AuthDao authDao;
    private final IdGenerator idGenerator;
    private final OffsetDateTimeProvider offsetDateTimeProvider;
    private final PropertySource propertySource;

    AccessToken login(String userName, String password, Boolean rememberMe) {
        User user = authDao.findUserByUserName(userName)
            .orElseThrow(() -> new UnauthorizedException("User not found with userName " + userName));
        Credentials credentials = user.getCredentials();

        if (!authDao.authenticate(password, credentials.getPassword())) {
            throw new UnauthorizedException("Bad password.");
        }

        if (!propertySource.isMultipleLoginAllowed()) {
            authDao.deleteAccessTokenByUserId(user.getUserId());
        }

        AccessToken accessToken = AccessToken.builder()
            .accessTokenId(idGenerator.generateRandomId())
            .userId(user.getUserId())
            .lastAccess(offsetDateTimeProvider.getCurrentDate())
            .isPersistent(Optional.ofNullable(rememberMe).orElse(false))
            .build();

        authDao.saveAccessToken(accessToken);
        return accessToken;
    }
}