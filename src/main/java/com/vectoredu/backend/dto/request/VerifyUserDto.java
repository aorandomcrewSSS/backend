package com.vectoredu.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
@AllArgsConstructor
public class VerifyUserDto {
    private String email;
    private String verificationCode;
}