package com.github.saphyra.authservice.service;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.Role;
import com.github.saphyra.authservice.domain.User;
import com.github.saphyra.util.OffsetDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.time.OffsetDateTime;
import java.util.Map;
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

    public AccessStatus canAccess(String requestUri, String userId, String accessTokenId) {
        Optional<AccessToken> accessTokenOptional = accessTokenCache.get(accessTokenId);
        if (!accessTokenOptional.isPresent()) {
            log.debug("Access token not found with accessTokenId {}", accessTokenId);
            return AccessStatus.UNAUTHORIZED;
        }

        AccessToken accessToken = accessTokenOptional.get();
        if (!userId.equals(accessToken.getUserId())) {
            log.warn("{} has no access to AccessToken {}", userId, accessTokenId);
            return AccessStatus.UNAUTHORIZED;
        }

        if (isAccessTokenExpired(accessToken)) {
            log.debug("AccessToken {} is expired.", accessTokenId);
            return AccessStatus.UNAUTHORIZED;
        }

        Optional<User> userOptional = authDao.findUserById(userId);
        if (!userOptional.isPresent()) {
            log.info("User not found with userId {}", userId);
            return AccessStatus.UNAUTHORIZED;
        }

        log.debug("AccessToken is valid. Updating lastAccess...");
        accessToken.setLastAccess(offsetDateTimeProvider.getCurrentDate());
        authDao.saveAccessToken(accessToken);

        return hasUserAccessForUri(requestUri, userOptional.get());
    }

    private boolean isAccessTokenExpired(AccessToken accessToken) {
        if (accessToken.isPersistent()) {
            return false;
        }
        OffsetDateTime expiration = accessToken.getLastAccess().plusMinutes(propertySource.getTokenExpirationMinutes());
        return offsetDateTimeProvider.getCurrentDate().isAfter(expiration);
    }

    private AccessStatus hasUserAccessForUri(String requestUri, User user) {
        Set<Role> userRoles = user.getRoles();
        log.debug("User roles: {}", userRoles);

        Optional<Map.Entry<String, Set<Role>>> matchingUriOptional = propertySource.getRoleSettings().entrySet().stream()
            .filter(entry -> antPathMatcher.match(entry.getKey(), requestUri))
            .findFirst();

        if (!matchingUriOptional.isPresent()) {
            log.debug("Request URI {} is not protected with Roles. Access Granted.", requestUri);
            return AccessStatus.GRANTED;
        }

        Set<Role> necessaryRoles = matchingUriOptional.get().getValue();
        log.debug("Necessary role(s) to access URI {} is {}", requestUri, necessaryRoles);
        boolean hasUserRole = necessaryRoles.stream()
            .anyMatch(userRoles::contains);

        log.debug("User has necessary role: {}", hasUserRole);
        return hasUserRole ? AccessStatus.GRANTED : AccessStatus.FORBIDDEN;
    }
}
