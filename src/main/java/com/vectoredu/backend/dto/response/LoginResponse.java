package com.vectoredu.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private long expiresIn;
}
