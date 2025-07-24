package com.example.loginotp.repository;

import com.example.loginotp.model.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {

    Optional<OtpRequest> findByPhone(String phone);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OtpRequest o WHERE o.phone = :phone")
    Optional<OtpRequest> findByPhoneWithLock(@Param("phone") String phone);
}
