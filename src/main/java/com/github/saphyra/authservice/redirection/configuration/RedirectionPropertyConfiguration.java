package com.github.saphyra.authservice.redirection.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class RedirectionPropertyConfiguration {
    @Value("${com.github.saphyra.authservice.redirection.filter.order:0}")
    private Integer filterOrder;
}
