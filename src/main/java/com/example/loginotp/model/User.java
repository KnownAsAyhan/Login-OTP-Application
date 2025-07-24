package com.example.loginotp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String phone;

    private String password;

    private int failedAttempts;

    private LocalDateTime blockUntil;

    private boolean isVerified;

    @Version
    private Integer version; // <--- Optimistic Locking
}
