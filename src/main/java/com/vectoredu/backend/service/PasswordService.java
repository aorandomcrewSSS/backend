package com.vectoredu.backend.service;

import com.vectoredu.backend.model.PasswordResetToken;
import com.vectoredu.backend.model.User;
import com.vectoredu.backend.repository.PasswordResetTokenRepository;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.util.exception.UserException;
import com.vectoredu.backend.util.exception.ValidationException;
import com.vectoredu.backend.util.validators.PasswordValidator;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PasswordService {
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final PasswordValidator passwordValidator;

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException("Пользователь не найден"));

        validateUserForPasswordReset(user);

        passwordResetTokenRepository.deleteByUser(user);

        String resetToken = generatePasswordResetToken();
        savePasswordResetToken(user, resetToken);

        String resetLink = "https://localhost:8080/auth/reset-password?token=" + resetToken;
        sendPasswordResetEmail(user, resetLink);
    }

    private void savePasswordResetToken(User user, String resetToken) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user, resetToken, LocalDateTime.now().plusMinutes(5));
        passwordResetTokenRepository.save(passwordResetToken);
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = validatePasswordResetToken(token);
        User user = passwordResetToken.getUser();

        validateNewPassword(newPassword);

        updatePassword(user, newPassword);
        passwordResetTokenRepository.deleteByToken(token);
    }

    private void validateNewPassword(String newPassword) {
        if (!passwordValidator.isValid(newPassword, null)) {
            throw new ValidationException("Пароль должен содержать хотя бы одну заглавную букву, одну цифру, быть не короче 8 и не длиннее 20 символов");
        }
    }

    private void updatePassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

    private PasswordResetToken validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ValidationException("Неверный или истекший токен для восстановления пароля"));

        if (passwordResetToken.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Токен для восстановления пароля истек");
        }

        return passwordResetToken;
    }

    private void validateUserForPasswordReset(User user) {
        if (user == null || !user.isEnabled()) {
            throw new UserException("Пользователь не найден или не активен");
        }
    }

    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }

    private void sendPasswordResetEmail(User user, String resetLink) {
        String subject = "Сброс пароля";
        String htmlMessage = generatePasswordResetEmailContent(resetLink);
        sendEmail(user, subject, htmlMessage);
    }

    private void sendEmail(User user, String subject, String htmlMessage) {
        try {
            emailService.sendVerificationEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке email", e);
        }
    }

    private String generatePasswordResetEmailContent(String resetLink) {
        return "<html>"
                + "<body style=\"font-family: Arial, sans-serif;\">"
                + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                + "<h2 style=\"color: #333;\">Welcome to our app!</h2>"
                + "<p style=\"font-size: 16px;\">Please follow the link in description to change your password:</p>"
                + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h3 style=\"color: #333;\">Link</h3>"
                + "<p style=\"font-size: 18px; font-weight: bold; color: #007bff;\">" + resetLink + "</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
    }
}
