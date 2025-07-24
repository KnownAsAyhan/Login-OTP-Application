package com.example.loginotp.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String phone;

    private String otpCode;

    private LocalDateTime expiresAt;

    private int requestCount;

    private LocalDateTime lastRequestTime;

    private int failedAttempts;

    private LocalDateTime blockUntil;

    @Version
    private Integer version; // <--- Optimistic Locking
}
