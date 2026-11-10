package com.example.elearning.repository;

import com.example.elearning.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByLearnerId(Long learnerId);
    List<QuizResult> findByQuizCourseInstructorId(Long instructorId);
    void deleteByQuizCourseId(Long courseId);
    void deleteByQuizId(Long quizId);
    void deleteByLearnerId(Long learnerId);
}
