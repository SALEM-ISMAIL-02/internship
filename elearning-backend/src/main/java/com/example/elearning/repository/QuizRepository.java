package com.example.elearning.repository;

import com.example.elearning.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCourseId(Long courseId);
    List<Quiz> findByCreatedById(Long userId);
    void deleteByCourseId(Long courseId);
}
