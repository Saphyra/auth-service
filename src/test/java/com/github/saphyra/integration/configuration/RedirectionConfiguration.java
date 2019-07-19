package com.github.saphyra.integration.configuration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@Import(MvcConfiguration.class)
public class RedirectionConfiguration {
}
