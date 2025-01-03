package com.vectoredu.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {
    private String email;
    private String password;
    private String username;
}
