package com.example.elearning.service;

import com.example.elearning.entity.User;
import com.example.elearning.repository.UserRepository;
import com.example.elearning.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> register(String name, String email, String password, User.Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role == null ? User.Role.LEARNER : role)
                .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    public Map<String, Object> login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return buildAuthResponse(user);
    }

    private Map<String, Object> buildAuthResponse(User user) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", jwtUtil.generateToken(user.getEmail(), user.getRole().name()));
        resp.put("id", user.getId());
        resp.put("name", user.getName());
        resp.put("email", user.getEmail());
        resp.put("role", user.getRole().name());
        return resp;
    }
}
