package com.vectoredu.backend.service.unit;

import com.vectoredu.backend.dto.request.LoginUserDto;
import com.vectoredu.backend.dto.request.RegisterUserDto;
import com.vectoredu.backend.dto.request.VerifyUserDto;
import com.vectoredu.backend.model.User;
import com.vectoredu.backend.repository.UserRepository;
import com.vectoredu.backend.service.AuthenticationService;
import com.vectoredu.backend.service.EmailService;
import com.vectoredu.backend.util.exception.KnownUseCaseException;
import com.vectoredu.backend.util.exception.UserException;
import com.vectoredu.backend.util.exception.ValidationException;
import com.vectoredu.backend.util.exception.VerificationException;
import com.vectoredu.backend.util.validators.EmailValidator;
import com.vectoredu.backend.util.validators.PasswordValidator;
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

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void signup_ShouldThrowValidationException_WhenEmailIsInvalid() {
        RegisterUserDto input = new RegisterUserDto("invalidemail", "username", "Password1");

        when(emailValidator.isValid(input.getEmail(), null)).thenReturn(false);

        assertThrows(ValidationException.class, () -> authenticationService.signup(input));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_ShouldThrowValidationException_WhenPasswordIsInvalid() {
        RegisterUserDto input = new RegisterUserDto("email@example.com", "username", "pass");

        when(emailValidator.isValid(input.getEmail(), null)).thenReturn(true);
        when(passwordValidator.isValid(input.getPassword(), null)).thenReturn(false);

        assertThrows(ValidationException.class, () -> authenticationService.signup(input));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_ShouldThrowKnownUseCaseException_WhenEmailAlreadyExists() {
        RegisterUserDto input = new RegisterUserDto("email@example.com", "username", "Password1");
        User existingUser = new User();
        existingUser.setEmail(input.getEmail());

        when(emailValidator.isValid(input.getEmail(), null)).thenReturn(true);
        when(passwordValidator.isValid(input.getPassword(), null)).thenReturn(true);
        when(userRepository.findByEmailOrUsername(input.getEmail(), input.getUsername())).thenReturn(Optional.of(existingUser));

        assertThrows(KnownUseCaseException.class, () -> authenticationService.signup(input));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void signup_ShouldSaveUser_WhenInputIsValid() {
        RegisterUserDto input = new RegisterUserDto("email@example.com", "username", "Password1");
        User userToSave = User.builder()
                .email(input.getEmail())
                .username(input.getUsername())
                .password("encodedPassword")
                .build();

        when(emailValidator.isValid(input.getEmail(), null)).thenReturn(true);
        when(passwordValidator.isValid(input.getPassword(), null)).thenReturn(true);
        when(userRepository.findByEmailOrUsername(input.getEmail(), input.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(input.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userToSave);

        User savedUser = authenticationService.signup(input);

        assertEquals(input.getEmail(), savedUser.getEmail());
        assertEquals(input.getUsername(), savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void authenticate_ShouldThrowUserException_WhenUserNotFound() {
        LoginUserDto input = new LoginUserDto("email@example.com", "Password1");

        when(userRepository.findByEmail(input.getEmail())).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> authenticationService.authenticate(input));
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
}
