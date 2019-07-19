package com.github.saphyra.authservice.auth.domain;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RestErrorResponse {
    private final HttpStatus httpStatus;
    private final Object responseBody;
}
