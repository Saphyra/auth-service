package com.github.saphyra.authservice.service;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.PropertySource;
import com.github.saphyra.authservice.domain.AccessStatus;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.authservice.domain.RoleSetting;
import com.github.saphyra.authservice.domain.User;
import com.github.saphyra.util.OffsetDateTimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
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

    public AccessStatus canAccess(String requestUri, HttpMethod method,  String userId, String accessTokenId) {
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

        return hasUserAccessForUri(requestUri, method, userOptional.get());
    }

    private boolean isAccessTokenExpired(AccessToken accessToken) {
        if (accessToken.isPersistent()) {
            return false;
        }
        OffsetDateTime expiration = accessToken.getLastAccess().plusMinutes(propertySource.getTokenExpirationMinutes());
        return offsetDateTimeProvider.getCurrentDate().isAfter(expiration);
    }

    private AccessStatus hasUserAccessForUri(String requestUri, HttpMethod method,  User user) {
        Set<String> userRoles = user.getRoles();
        log.debug("User roles: {}", userRoles);

        Optional<RoleSetting> matchingRoleSettingOptional = propertySource.getRoleSettings().stream()
            .filter(roleSetting -> antPathMatcher.match(roleSetting.getUri(), requestUri))
            .findFirst();

        if (!matchingRoleSettingOptional.isPresent()) {
            log.debug("Request URI {} is not protected with Roles. Access Granted.", requestUri);
            return AccessStatus.GRANTED;
        }

        RoleSetting roleSetting = matchingRoleSettingOptional.get();
        if(!roleSetting.getProtectedMethods().contains(method)){
            log.debug("URI {} with method {} is not protected.", requestUri, method);
            return AccessStatus.GRANTED;
        }

        Set<String> necessaryRoles = roleSetting.getRoles();
        log.debug("Necessary role(s) to access URI {} is {}", requestUri, necessaryRoles);
        boolean hasUserRole = necessaryRoles.stream()
            .anyMatch(userRoles::contains);

        log.debug("User has necessary role: {}", hasUserRole);
        return hasUserRole ? AccessStatus.GRANTED : AccessStatus.FORBIDDEN;
    }
}
