package com.example.loginotp.service;

import com.example.loginotp.model.OtpRequest;
import com.example.loginotp.repository.OtpRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRequestRepository otpRepo;

    public String sendOtp(String phone) {
        OtpRequest otp = otpRepo.findByPhone(phone).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        // If user is blocked from requesting
        if (otp != null && otp.getBlockUntil() != null && now.isBefore(otp.getBlockUntil())) {
            long secondsLeft = java.time.Duration.between(now, otp.getBlockUntil()).getSeconds();
            return "OTP blocked for " + secondsLeft + " seconds due to too many requests.";
        }

        // Create or update existing request
        if (otp == null) {
            otp = OtpRequest.builder()
                    .phone(phone)
                    .requestCount(1)
                    .lastRequestTime(now)
                    .otpCode(generateOtp())
                    .expiresAt(now.plusMinutes(5))
                    .build();
        } else {
            // Check if 3 minutes passed since last request
            if (otp.getLastRequestTime() != null && now.minusMinutes(3).isAfter(otp.getLastRequestTime())) {
                otp.setRequestCount(1); // reset counter
            } else {
                // Too many requests
                if (otp.getRequestCount() >= 5) {
                    otp.setBlockUntil(now.plusMinutes(3));
                    otp.setRequestCount(0);
                    otpRepo.save(otp);
                    return "OTP request limit reached. Try again in 3 minutes.";
                }
                otp.setRequestCount(otp.getRequestCount() + 1);
            }

            otp.setOtpCode(generateOtp());
            otp.setExpiresAt(now.plusMinutes(5));
            otp.setLastRequestTime(now);
        }

        otpRepo.save(otp);
        System.out.println("Generated OTP for " + phone + ": " + otp.getOtpCode()); // simulate SMS
        return "OTP sent to " + phone + ". Your code is: " + otp.getOtpCode();
    }

    private String generateOtp() {
        int otp = new Random().nextInt(9000) + 1000; // 4-digit random OTP
        return String.valueOf(otp);
    }

    public String verifyOtp(String phone, String code) {
        OtpRequest otp = otpRepo.findByPhone(phone).orElse(null);
        LocalDateTime now = LocalDateTime.now();

        if (otp == null) {
            return "No OTP request found for this phone.";
        }

        // Block check for failed OTP
        if (otp.getBlockUntil() != null && now.isBefore(otp.getBlockUntil())) {
            long sec = java.time.Duration.between(now, otp.getBlockUntil()).getSeconds();
            return "Blocked due to wrong attempts. Try again in " + sec + " seconds.";
        }

        // ✅ Wrong code check FIRST
        if (!otp.getOtpCode().equals(code)) {
            otp.setFailedAttempts(otp.getFailedAttempts() + 1);
            otp.setBlockUntil(now.plusMinutes(1));
            otpRepo.save(otp);
            return "Wrong OTP. Blocked for 1 minute.";
        }

        // ✅ Expiration check AFTER code is confirmed correct
        if (otp.getExpiresAt() == null || now.isAfter(otp.getExpiresAt())) {
            return "OTP has expired. Please request a new one.";
        }

        // ✅ OTP is correct
        otp.setFailedAttempts(0);
        otp.setBlockUntil(null);
        otpRepo.save(otp);

        return "OTP verified successfully.";
    }



}
