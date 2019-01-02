package com.github.saphyra.authservice.controller.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class LoginRequest {
    @NotNull
    private String userName;

    @NotNull
    private String password;
}
