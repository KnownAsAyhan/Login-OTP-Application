package com.example.loginotp.service;

import com.example.loginotp.model.User;
import com.example.loginotp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public String login(String phone, String password) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getBlockUntil() != null && user.getBlockUntil().isAfter(LocalDateTime.now())) {
            return "User is blocked. Try again at: " + user.getBlockUntil();
        }

        if (!password.equals(user.getPassword())) {
            int attempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(attempts);

            if (attempts >= 3) {
                user.setBlockUntil(LocalDateTime.now().plusSeconds(30));
                user.setFailedAttempts(0); // reset after blocking
                userRepository.save(user);
                return "Too many failed attempts. Blocked for 30 seconds.";
            }

            userRepository.save(user);
            return "Wrong password. Attempt " + attempts + "/3.";
        }

        // Login successful
        user.setFailedAttempts(0);
        user.setBlockUntil(null);
        userRepository.save(user);

        // Simulate SMS
        return "Login successful. (SMS sent)";
    }
}
