package com.github.saphyra.authservice.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class PropertyConfiguration {
    @Value("${com.github.saphyra.authservice.access-token.expiration-seconds}")
    private long expirationSeconds;
}
