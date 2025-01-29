package com.vectoredu.backend.service.unit;

import com.vectoredu.backend.dto.request.LoginUserDto;
import com.vectoredu.backend.dto.request.RegisterUserDto;
import com.vectoredu.backend.dto.request.VerifyUserDto;
import com.vectoredu.backend.model.User;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.service.authservice.AuthenticationService;
import com.vectoredu.backend.service.authservice.EmailService;
import com.vectoredu.backend.service.authservice.PasswordService;
import com.vectoredu.backend.util.exception.NotFoundException;
import com.vectoredu.backend.util.exception.ValidationException;
import com.vectoredu.backend.util.exception.VerificationException;
import com.vectoredu.backend.util.validators.EmailValidator;
import com.vectoredu.backend.util.validators.PasswordValidator;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailValidator emailValidator;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signup_ShouldThrowValidationException_WhenEmailIsInvalid() {
        RegisterUserDto input = new RegisterUserDto("invalidemail", "Test", "User", "Password1");

        when(emailValidator.isValid(input.getEmail(), null)).thenReturn(false);

        assertThrows(ValidationException.class, () -> authenticationService.signup(input));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_ShouldThrowValidationException_WhenPasswordIsInvalid() {
        RegisterUserDto input = new RegisterUserDto("email@example.com", "Test", "User", "pass");

        when(emailValidator.isValid(input.getEmail(), null)).thenReturn(true);
        when(passwordValidator.isValid(input.getPassword(), null)).thenReturn(false);

        assertThrows(ValidationException.class, () -> authenticationService.signup(input));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_ShouldSaveUser_WhenInputIsValid() {
        RegisterUserDto input = new RegisterUserDto("email@example.com", "Test", "User", "Password1");
        User userToSave = User.builder()
                .firstName(input.getFirstName())
                .lastName(input.getLastName())
                .email(input.getEmail())
                .password("encodedPassword")
                .verificationCode("123456")
                .verificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15))
                .enabled(false)
                .build();

        when(emailValidator.isValid(input.getEmail(), null)).thenReturn(true);
        when(passwordValidator.isValid(input.getPassword(), null)).thenReturn(true);
        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(input.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        User savedUser = authenticationService.signup(input);

        assertEquals(input.getEmail(), savedUser.getEmail());
        assertEquals(input.getFirstName(), savedUser.getFirstName());
        assertEquals(input.getLastName(), savedUser.getLastName());
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_ShouldThrowUserException_WhenUserNotFound() {
        LoginUserDto input = new LoginUserDto("email@example.com", "Password1");

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> authenticationService.authenticate(input));
    }

    @Test
    void authenticate_ShouldThrowVerificationException_WhenUserIsNotEnabled() {
        LoginUserDto input = new LoginUserDto("email@example.com", "Password1");
        User user = new User();
        user.setEnabled(false);

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(user));

        assertThrows(VerificationException.class, () -> authenticationService.authenticate(input));
    }

    @Test
    void verifyUser_ShouldEnableUser_WhenVerificationCodeIsValid() {
        VerifyUserDto input = new VerifyUserDto("email@example.com", "123456");
        User user = new User();
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(user));

        authenticationService.verifyUser(input);

        assertTrue(user.isEnabled());
        assertNull(user.getVerificationCode());
        assertNull(user.getVerificationCodeExpiresAt());
        verify(userRepository).save(user);
    }

    @Test
    void verifyUser_ShouldThrowValidationException_WhenVerificationCodeIsInvalid() {
        VerifyUserDto input = new VerifyUserDto("email@example.com", "wrongCode");
        User user = new User();
        user.setVerificationCode("123456");
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(10));

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.of(user));

        assertThrows(ValidationException.class, () -> authenticationService.verifyUser(input));
    }

    @Test
    void resendVerificationCode_ShouldSendNewCode_WhenUserIsNotVerified() throws MessagingException {
        String email = "email@example.com";
        User user = new User();
        user.setEmail(email);
        user.setEnabled(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        authenticationService.resendVerificationCode(email);

        verify(userRepository).save(any(User.class));
        verify(emailService).sendVerificationEmail(any(), any(), any());
    }
}
