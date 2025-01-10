package com.vectoredu.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity(name = "ResetPassword")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(unique = true)
    private String token;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    public PasswordResetToken(User user, String token, LocalDateTime expirationDate) {
        this.user = user;
        this.token = token;
        this.expirationDate = expirationDate;
    }
}
