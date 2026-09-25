package com.example.elearning.repository;

import com.example.elearning.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByLearnerId(Long learnerId);
}
