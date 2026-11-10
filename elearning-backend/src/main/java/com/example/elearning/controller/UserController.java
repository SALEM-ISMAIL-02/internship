package com.example.elearning.controller;

import com.example.elearning.entity.User;
import com.example.elearning.service.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserAdminService userAdminService;

    @GetMapping
    public List<User> list() {
        return userAdminService.list();
    }

    @PostMapping
    public ResponseEntity<User> create(@RequestBody Map<String, String> body) {
        User.Role role = body.get("role") == null
                ? User.Role.LEARNER : User.Role.valueOf(body.get("role").toUpperCase());
        User user = userAdminService.create(
                body.get("name"), body.get("email"), body.get("password"), role);
        user.setPasswordHash(null);
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            userAdminService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
