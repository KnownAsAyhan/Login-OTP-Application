package com.example.loginotp.controller;

import com.example.loginotp.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/send")
    public String sendOtp(@RequestParam String phone) {
        return otpService.sendOtp(phone);
    }

    @PostMapping("/verify")
    public String verifyOtp(@RequestParam String phone, @RequestParam String code) {
        return otpService.verifyOtp(phone, code);
    }

}
