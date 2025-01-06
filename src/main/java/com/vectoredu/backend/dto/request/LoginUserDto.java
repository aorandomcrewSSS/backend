package com.vectoredu.backend.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class LoginUserDto {
    private String email;
    private String password;
}
