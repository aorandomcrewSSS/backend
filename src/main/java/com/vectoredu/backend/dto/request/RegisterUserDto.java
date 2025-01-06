package com.vectoredu.backend.dto.request;

import com.vectoredu.backend.util.validators.ValidEmail;
import com.vectoredu.backend.util.validators.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain=true)
public class RegisterUserDto {

    @ValidEmail
    private String email;

    @ValidPassword
    private String password;

    @NotBlank(message = "имя пользователя не может быть пустым")
    private String username;
}
