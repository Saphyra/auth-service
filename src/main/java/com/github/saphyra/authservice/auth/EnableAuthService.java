package com.github.saphyra.authservice.auth;

import com.github.saphyra.authservice.auth.configuration.AuthServiceBeanConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(AuthServiceBeanConfiguration.class)
public @interface EnableAuthService {
}
