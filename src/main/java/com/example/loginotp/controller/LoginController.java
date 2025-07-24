package com.example.loginotp.controller;

import com.example.loginotp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class LoginController {

    private final UserService userService;

    @PostMapping("/login")
    public String login(@RequestParam String phone, @RequestParam String password) {
        return userService.login(phone, password);
    }
}
