package com.github.saphyra.authservice.service;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.Credentials;
import com.github.saphyra.authservice.domain.User;
import com.github.saphyra.encryption.impl.PasswordService;
import com.github.saphyra.exceptionhandling.exception.UnauthorizedException;
import com.github.saphyra.util.IdGenerator;
import com.github.saphyra.util.OffsetDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
//TODO unit test
public class LoginService {
    private final AuthDao authDao;
    private final IdGenerator idGenerator;
    private final OffsetDateTimeProvider offsetDateTimeProvider;
    private final PasswordService passwordService;
    private final PropertySource propertySource;

    public AccessToken login(String userName, String password) {
        User user = authDao.findUserByUserName(userName)
            .orElseThrow(() -> new UnauthorizedException("User not found with userName " + userName));
        Credentials credentials = user.getCredentials();

        if (!passwordService.authenticate(password, credentials.getPassword())) {
            throw new UnauthorizedException("Bad password.");
        }

        if (!propertySource.isMultipleLoginAllowed()) {
            authDao.deleteAccessTokenByUserId(user.getUserId());
        }

        AccessToken accessToken = new AccessToken(idGenerator.generateRandomId(), user.getUserId(), offsetDateTimeProvider.getCurrentDate());
        authDao.saveAccessToken(accessToken);
        return accessToken;
    }
}