package com.vectoredu.backend.service.unit;

import com.vectoredu.backend.model.PasswordResetToken;
import com.vectoredu.backend.model.User;
import com.vectoredu.backend.repository.PasswordResetTokenRepository;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.service.authservice.EmailService;
import com.vectoredu.backend.service.authservice.PasswordService;
import com.vectoredu.backend.util.exception.NotFoundException;
import com.vectoredu.backend.util.exception.ValidationException;
import com.vectoredu.backend.util.validators.PasswordValidator;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordValidator passwordValidator;

    @InjectMocks
    private PasswordService passwordService;

    private User user;
    private PasswordResetToken passwordResetToken;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("email@example.com");
        user.setEnabled(true);
        user.setPassword("encodedPassword");

        passwordResetToken = new PasswordResetToken(user, UUID.randomUUID().toString(), LocalDateTime.now().plusMinutes(5));
    }

    @Test
    void requestPasswordReset_ShouldThrowUserException_WhenUserNotFound() throws MessagingException {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> passwordService.requestPasswordReset(email));
        verify(passwordResetTokenRepository, never()).deleteByUser(any());
        verify(emailService, never()).sendVerificationEmail(any(), any(), any());
    }

    @Test
    void resetPassword_ShouldThrowValidationException_WhenTokenIsExpired() {
        String token = "expiredToken";
        passwordResetToken.setExpirationDate(LocalDateTime.now().minusMinutes(1));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(passwordResetToken));

        assertThrows(ValidationException.class, () -> passwordService.resetPassword(token, "NewPassword1"));
    }


    @Test
    void resetPassword_ShouldThrowValidationException_WhenNewPasswordIsInvalid() {
        String token = "validToken";
        String invalidPassword = "short";

        passwordResetToken.setExpirationDate(LocalDateTime.now().plusMinutes(5));

        when(passwordResetTokenRepository.findByToken(token)).thenReturn(Optional.of(passwordResetToken));

        when(passwordValidator.isValid(invalidPassword, null)).thenReturn(false);

        assertThrows(ValidationException.class, () -> passwordService.resetPassword(token, invalidPassword));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void validateNewPassword_ShouldThrowValidationException_WhenPasswordIsInvalid() {
        String invalidPassword = "short";

        when(passwordValidator.isValid(invalidPassword, null)).thenReturn(false);

        assertThrows(ValidationException.class, () -> passwordService.validateNewPassword(invalidPassword));
    }

    @Test
    void validateNewPassword_ShouldPass_WhenPasswordIsValid() {
        String validPassword = "ValidPassword123";

        when(passwordValidator.isValid(validPassword, null)).thenReturn(true);

        passwordService.validateNewPassword(validPassword);
    }

    @Test
    void sendPasswordResetEmail_ShouldSendEmail() throws MessagingException {
        String resetLink = "https://localhost:8080/auth/reset-password?token=reset-token";

        doNothing().when(emailService).sendVerificationEmail(any(), any(), any());

        passwordService.sendPasswordResetEmail(user, resetLink);

        verify(emailService).sendVerificationEmail(eq(user.getEmail()), anyString(), anyString());
    }
}
