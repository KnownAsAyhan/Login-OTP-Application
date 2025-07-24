package com.example.loginotp.service;

import com.example.loginotp.model.OtpRequest;
import com.example.loginotp.repository.OtpRequestRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRequestRepository otpRepo;

    @Transactional
    public String sendOtp(String phone) {
        LocalDateTime now = LocalDateTime.now();

        OtpRequest otp = otpRepo.findByPhoneWithLock(phone).orElse(null);

        if (otp != null && otp.getBlockUntil() != null && now.isBefore(otp.getBlockUntil())) {
            long secondsLeft = java.time.Duration.between(now, otp.getBlockUntil()).getSeconds();
            return "OTP blocked for " + secondsLeft + " seconds due to too many requests.";
        }

        if (otp == null) {
            otp = OtpRequest.builder()
                    .phone(phone)
                    .requestCount(1)
                    .lastRequestTime(now)
                    .otpCode(generateOtp())
                    .expiresAt(now.plusMinutes(5))
                    .failedAttempts(0)
                    .build();
        } else {
            if (otp.getLastRequestTime() != null && now.minusMinutes(3).isAfter(otp.getLastRequestTime())) {
                otp.setRequestCount(1); // reset count
            } else {
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
        System.out.println("Generated OTP for " + phone + ": " + otp.getOtpCode());
        return "OTP sent to " + phone + ". Your code is: " + otp.getOtpCode();
    }

    @Transactional
    public String verifyOtp(String phone, String code) {
        LocalDateTime now = LocalDateTime.now();

        OtpRequest otp = otpRepo.findByPhoneWithLock(phone).orElse(null);

        if (otp == null) {
            return "No OTP request found for this phone.";
        }

        if (otp.getBlockUntil() != null && now.isBefore(otp.getBlockUntil())) {
            long sec = java.time.Duration.between(now, otp.getBlockUntil()).getSeconds();
            return "Blocked due to wrong attempts. Try again in " + sec + " seconds.";
        }

        if (!otp.getOtpCode().equals(code)) {
            otp.setFailedAttempts(otp.getFailedAttempts() + 1);
            otp.setBlockUntil(now.plusMinutes(1));
            otpRepo.save(otp);
            return "Wrong OTP. Blocked for 1 minute.";
        }

        if (otp.getExpiresAt() == null || now.isAfter(otp.getExpiresAt())) {
            return "OTP has expired. Please request a new one.";
        }

        otp.setFailedAttempts(0);
        otp.setBlockUntil(null);
        otpRepo.save(otp);
        return "OTP verified successfully.";
    }

    private String generateOtp() {
        return String.valueOf(new Random().nextInt(9000) + 1000);
    }
}
