package com.vectoredu.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@Accessors(chain=true)
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Instant expiresIn;

    private String refreshToken;
    private Instant refreshExpiresIn;
}
