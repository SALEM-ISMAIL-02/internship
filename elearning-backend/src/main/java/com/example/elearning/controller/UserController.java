package com.example.elearning.controller;

import com.example.elearning.entity.User;
import com.example.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    public List<User> list() {
        List<User> users = userRepository.findAll();
        users.forEach(u -> u.setPasswordHash(null));
        return users;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}
