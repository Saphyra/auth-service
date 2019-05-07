package com.github.saphyra.authservice.impl;

import com.github.saphyra.authservice.AuthDao;
import com.github.saphyra.authservice.domain.AccessToken;
import com.github.saphyra.cache.AbstractCache;
import com.google.common.cache.CacheBuilder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
class AccessTokenCache extends AbstractCache<String, AccessToken> {
    private final AuthDao authDao;

    public AccessTokenCache(AuthDao authDao) {
        super(
            CacheBuilder.newBuilder()
                .expireAfterAccess(2, TimeUnit.SECONDS)
                .build()
        );
        this.authDao = authDao;
    }

    @Override
    public Optional<AccessToken> get(String key) {
        return get(key, () -> authDao.findAccessTokenByTokenId(key));
    }
}
