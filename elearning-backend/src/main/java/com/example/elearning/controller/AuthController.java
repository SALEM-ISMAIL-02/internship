package com.example.elearning.controller;

import com.example.elearning.entity.User;
import com.example.elearning.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> body) {
        User.Role role = body.get("role") == null ? null : User.Role.valueOf(body.get("role").toUpperCase());
        return ResponseEntity.ok(authService.register(
                body.get("name"), body.get("email"), body.get("password"), role));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(authService.login(body.get("email"), body.get("password")));
    }
}
