package com.vectoredu.backend.dto.request;

import com.vectoredu.backend.util.validators.ValidEmail;
import com.vectoredu.backend.util.validators.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDto {

    @ValidEmail
    private String email;

    @ValidPassword
    private String password;

    @NotBlank(message = "имя пользователя не может быть пустым")
    private String username;
}
