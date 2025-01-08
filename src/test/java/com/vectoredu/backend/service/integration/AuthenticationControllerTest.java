package com.vectoredu.backend.service.integration;

import com.vectoredu.backend.service.config.AbstractIntegrationTest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@AutoConfigureMockMvc
@Tag(name = "Аутентификация", description = "Операции, связанные с аутентификацией")
public class AuthenticationControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    public void clearDatabase() {
        // Очистка всех записей в нужных таблицах
        jdbcTemplate.execute("DELETE FROM users"); // Пример для очистки таблицы пользователей

    }

    @BeforeEach
    public void setup() throws Exception {
        // Создайте пользователя перед каждым тестом
        String json = "{\"email\":\"test@example.com\", \"password\":\"Password123\", \"username\":\"testuser\"}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    public void testRegisterUser() throws Exception {
        String json = "{\"email\":\"test1@example.com\", \"password\":\"Password123\", \"username\":\"testuser1\"}";

        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test1@example.com"))
                .andExpect(jsonPath("$.username").value("testuser1"));
    }

    @Test
    public void testVerifyUser() throws Exception {
        String email = "test@example.com";

        String verificationCode = jdbcTemplate.queryForObject(
                "SELECT verification_code FROM users WHERE email = ?",
                new Object[]{email},
                String.class
        );

        String json = String.format("{\"email\":\"%s\", \"verificationCode\":\"%s\"}", email, verificationCode);

        mockMvc.perform(post("/auth/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Аккаунт успешно подтвержден"));
    }

    @Test
    public void testSuccessLoginUser() throws Exception {
        String email = "test@example.com";
        String password = "Password123";

        jdbcTemplate.update("UPDATE users SET enabled = TRUE WHERE email = ?", email);

        String json = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    public void testResendVerificationCode() throws Exception {
        mockMvc.perform(post("/auth/resend")
                        .param("email", "test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Код для подтверждения отправлен"));
    }
}
