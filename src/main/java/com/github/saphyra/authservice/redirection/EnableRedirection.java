package com.github.saphyra.authservice.redirection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.github.saphyra.authservice.redirection.configuration.RedirectionBeanConfiguration;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(RedirectionBeanConfiguration.class)
public @interface EnableRedirection {
}
