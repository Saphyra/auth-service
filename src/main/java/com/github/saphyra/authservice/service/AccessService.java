package com.github.saphyra.authservice.service;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.Role;
import com.github.saphyra.authservice.domain.User;
import com.github.saphyra.util.OffsetDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccessService {
    private final AccessTokenCache accessTokenCache;
    private final AntPathMatcher antPathMatcher;
    private final AuthDao authDao;
    private final OffsetDateTimeProvider offsetDateTimeProvider;
    private final PropertySource propertySource;

    //todo unit test
    public boolean canAccess(String requestUri, String userId, String accessTokenId) {
        Optional<AccessToken> accessTokenOptional = accessTokenCache.get(accessTokenId);
        if (!accessTokenOptional.isPresent()) {
            return false;
        }

        AccessToken accessToken = accessTokenOptional.get();
        if (!userId.equals(accessToken.getUserId())) {
            return false;
        }

        if (isAccessTokenExpired(accessToken.getLastAccess())) {
            return false;
        }

        Optional<User> userOptional = authDao.findUserById(userId);
        if (!userOptional.isPresent()) {
            return false;
        }

        accessToken.setLastAccess(offsetDateTimeProvider.getCurrentDate());
        authDao.saveAccessToken(accessToken);

        return hasUserAccessForUri(requestUri, userOptional.get());
    }

    private boolean isAccessTokenExpired(OffsetDateTime lastAccess) {
        OffsetDateTime expiration = lastAccess.plusMinutes(propertySource.getTokenExpirationMinutes());
        return offsetDateTimeProvider.getCurrentDate().isAfter(expiration);
    }

    private boolean hasUserAccessForUri(String requestUri, User user) {
        Set<Role> roles = user.getRoles();
        return propertySource.getRoleSettings().entrySet().stream()
            .filter(entry -> antPathMatcher.match(entry.getKey(), requestUri))
            .findFirst()
            .map(entry -> entry.getValue().stream()
                .anyMatch(roles::contains))
            .orElse(true);
    }
}
