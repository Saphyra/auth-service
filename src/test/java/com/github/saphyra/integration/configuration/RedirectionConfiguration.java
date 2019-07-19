package com.github.saphyra.integration.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import com.github.saphyra.authservice.redirection.EnableRedirection;

@TestConfiguration
@Import(MvcConfiguration.class)
@EnableRedirection
public class RedirectionConfiguration {
}
