package com.github.saphyra.authservice.domain;

import lombok.Data;

@Data
public class Credentials {
    private String userId;
    private String userName;
    private String password;
}
