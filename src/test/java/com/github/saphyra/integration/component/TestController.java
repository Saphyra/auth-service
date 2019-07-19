package com.github.saphyra.integration.component;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    public static final String ALLOWED_URI_MAPPING = "/allowed-uri";
    public static final String PROTECTED_URI_MAPPING = "/protected-uri";

    @RequestMapping(ALLOWED_URI_MAPPING)
    public String allowedUri() {
        return ALLOWED_URI_MAPPING;
    }

    @RequestMapping(PROTECTED_URI_MAPPING)
    public String protectedUri() {
        return PROTECTED_URI_MAPPING;
    }
}
