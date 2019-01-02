package com.github.saphyra.authservice.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(com.github.saphyra.encryption.configuration.BeanConfig.class)
@ComponentScan(basePackages = "com.github.saphyra.authservice")
public class BeanConfig {
}
