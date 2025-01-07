package com.vectoredu.backend.service;

import com.vectoredu.backend.dto.request.LoginUserDto;
import com.vectoredu.backend.dto.request.RegisterUserDto;
import com.vectoredu.backend.dto.request.VerifyUserDto;
import com.vectoredu.backend.model.User;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.util.exception.*;
import com.vectoredu.backend.util.validators.EmailValidator;
import com.vectoredu.backend.util.validators.PasswordValidator;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    private final EmailValidator emailValidator;
    private final PasswordValidator passwordValidator;

    public User signup(RegisterUserDto input) {
        validateSignupInput(input);
        checkUserExistence(input);
        User userToCreate = createUser(input);
        return userRepository.save(userToCreate);
    }

    public User authenticate(LoginUserDto input) {
        User user = findUserByEmail(input.getEmail());
        checkUserEnabled(user);
        authenticateUser(input);
        return user;
    }

    public void verifyUser(VerifyUserDto input) {
        User user = findUserByEmail(input.getEmail());
        validateVerificationCode(user, input);
        enableUser(user);
    }

    public void resendVerificationCode(String email) {
        User user = findUserByEmail(email);
        checkUserAlreadyVerified(user);
        updateUserVerificationCode(user);
        sendVerificationEmail(user);
    }

    private void validateSignupInput(RegisterUserDto input) {
        if (!emailValidator.isValid(input.getEmail(), null)) {
            throw new ValidationException("Не верный формат email");
        }
        if (!passwordValidator.isValid(input.getPassword(), null)) {
            throw new ValidationException("Пароль должен содержать хотя бы одну заглавную букву, одну цифру, быть не короче 8 и не длиннее 20 символов");
        }
    }

    private void checkUserExistence(RegisterUserDto input) {
        Optional<User> user = userRepository.findByEmailOrUsername(input.getEmail(), input.getUsername());
        if (user.isPresent()) {
            if (user.get().getEmail().equals(input.getEmail())) {
                throw new KnownUseCaseException("Такая почта уже зарегистрирована");
            }
            if (user.get().getUsername().equals(input.getUsername())) {
                throw new KnownUseCaseException("Такое имя пользователя уже зарегистрировано");
            }
        }
    }

    private User createUser(RegisterUserDto input) {
        String encodedPassword = passwordEncoder.encode(input.getPassword());
        return User.builder()
                .username(input.getUsername())
                .email(input.getEmail())
                .password(encodedPassword)
                .verificationCode(generateVerificationCode())
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15))
                .enabled(false)
                .build();
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("Пользователь не найден"));
    }

    private void checkUserEnabled(User user) {
        if (!user.isEnabled()) {
            throw new VerificationException("Пользователь не верифицирован");
        }
    }

    private void authenticateUser(LoginUserDto input) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(input.getEmail(), input.getPassword())
        );
    }

    private void validateVerificationCode(User user, VerifyUserDto input) {
        if (user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Время подтверждения истекло");
        }
        if (!user.getVerificationCode().equals(input.getVerificationCode())) {
            throw new ValidationException("Неверный код подтверждения");
        }
    }

    private void enableUser(User user) {
        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);
    }

    private void checkUserAlreadyVerified(User user) {
        if (user.isEnabled()) {
            throw new KnownUseCaseException("Аккаунт уже подтвержден");
        }
    }

    private void updateUserVerificationCode(User user) {
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
    }

    private void sendVerificationEmail(User user) {
        String subject = "Account Verification";
        String verificationCode = "VERIFICATION CODE " + user.getVerificationCode();
        String htmlMessage = generateVerificationEmailContent(verificationCode);
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            log.info("ошибка отправки кода верификации");
        }
    }

    private String generateVerificationEmailContent(String verificationCode) {
        return "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please enter the verification code below to continue:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Verification Code:</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + verificationCode + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
