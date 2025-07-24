package com.example.loginotp.repository;

import com.example.loginotp.model.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {
    Optional<OtpRequest> findByPhone(String phone);
}
