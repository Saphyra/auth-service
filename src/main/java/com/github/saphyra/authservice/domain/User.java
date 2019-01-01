package com.github.saphyra.authservice.domain;

import lombok.Data;

import java.util.Set;

@Data
public class User {
    private String userId;
    private Credentials credentials;
    private Set<Role> roles;
}
