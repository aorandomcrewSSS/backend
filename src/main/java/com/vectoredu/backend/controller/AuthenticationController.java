package com.vectoredu.backend.controller;

import com.vectoredu.backend.dto.request.LoginUserDto;
import com.vectoredu.backend.dto.request.RegisterUserDto;
import com.vectoredu.backend.dto.request.VerifyUserDto;
import com.vectoredu.backend.dto.response.LoginResponse;
import com.vectoredu.backend.model.User;
import com.vectoredu.backend.service.AuthenticationService;
import com.vectoredu.backend.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Аутентификация", description = "Операции, связанные с аутентификацией")
@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @Operation(summary = "Регистрация нового пользователя", responses = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Неверные данные")
    })
    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);
        return ResponseEntity.ok(registeredUser);
    }

    @Operation(summary = "Аутентификация пользователя и получение JWT", responses = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно аутентифицирован"),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    // Логика аутентификации и получения JWT токенов
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto){
        LoginResponse loginResponse = authenticationService.authenticate(loginUserDto);
        return ResponseEntity.ok(loginResponse);
    }

    @Operation(summary = "Обновление access-токена", responses = {
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Ошибка валидации токена")
    })
    // Логика получения нового access токена по refresh токену
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshAccessToken(@RequestParam String refreshToken){
        String newAccessToken = authenticationService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(newAccessToken);
    }

    @Operation(summary = "Подтверждение аккаунта пользователя", responses = {
            @ApiResponse(responseCode = "200", description = "Аккаунт успешно подтвержден"),
            @ApiResponse(responseCode = "400", description = "Неверные данные для подтверждения")
    })
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDto verifyUserDto) {
        authenticationService.verifyUser(verifyUserDto);
        return ResponseEntity.ok("Аккаунт успешно подтвержден");
    }

    @Operation(summary = "Повторная отправка кода для подтверждения аккаунта", responses = {
            @ApiResponse(responseCode = "200", description = "Код для подтверждения успешно отправлен"),
            @ApiResponse(responseCode = "400", description = "Ошибка при отправке кода")
    })
    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email) {
        authenticationService.resendVerificationCode(email);
        return ResponseEntity.ok("Код для подтверждения отправлен");
    }

    @Operation(summary = "Запрос на восстановление пароля", responses = {
            @ApiResponse(responseCode = "200", description = "Ссылка для сброса пароля отправлена на вашу почту"),
            @ApiResponse(responseCode = "400", description = "Неверные данные")
    })
    @PostMapping("/request-password-reset")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        authenticationService.requestPasswordReset(email);
        return ResponseEntity.ok("Ссылка для сброса пароля отправлена на вашу почту");
    }

    @Operation(summary = "форма для восстановление пароля", responses = {
            @ApiResponse(responseCode = "200", description = "пароль успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Неверные данные")
    })
    @PatchMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authenticationService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Пароль успешно изменен");
    }
}