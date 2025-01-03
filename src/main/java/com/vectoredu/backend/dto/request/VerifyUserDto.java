package com.vectoredu.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyUserDto {
    private String email;
    private String verificationCode;
}