package com.example.elearning.service;

import com.example.elearning.entity.User;
import com.example.elearning.repository.UserRepository;
import com.example.elearning.repository.LearnerProgressRepository;
import com.example.elearning.repository.QuizResultRepository;
import com.example.elearning.repository.CourseRepository;
import com.example.elearning.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * SRP: admin user-management use-cases (passwords are never exposed).
 * DIP: controllers depend on this, never on {@code UserRepository}.
 */
@Service
@RequiredArgsConstructor
public class UserAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LearnerProgressRepository progressRepository;
    private final QuizResultRepository quizResultRepository;
    private final CourseRepository courseRepository;
    private final QuizRepository quizRepository;

    public User create(String name, String email, String password, User.Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        return userRepository.save(User.builder()
                .name(name)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(role == null ? User.Role.LEARNER : role)
                .build());
    }

    @Transactional(readOnly = true)
    public List<User> list() {
        // Never null the hash on the managed entity (would dirty-check to NULL).
        // Return detached copies with the secret stripped.
        return userRepository.findAll().stream()
                .map(u -> User.builder()
                        .id(u.getId())
                        .name(u.getName())
                        .email(u.getEmail())
                        .passwordHash(null)
                        .role(u.getRole())
                        .build())
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        quizResultRepository.deleteByLearnerId(id);
        progressRepository.deleteByLearnerId(id);
        var ownedCourses = courseRepository.findByInstructorId(id);
        ownedCourses.forEach(course -> course.setInstructor(null));
        courseRepository.saveAll(ownedCourses);
        var authoredQuizzes = quizRepository.findByCreatedById(id);
        authoredQuizzes.forEach(quiz -> quiz.setCreatedBy(null));
        quizRepository.saveAll(authoredQuizzes);
        userRepository.deleteById(id);
    }
}
