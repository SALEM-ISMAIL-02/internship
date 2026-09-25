package com.example.elearning.repository;

import com.example.elearning.entity.LearnerProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LearnerProgressRepository extends JpaRepository<LearnerProgress, Long> {
    List<LearnerProgress> findByLearnerId(Long learnerId);
    Optional<LearnerProgress> findByLearnerIdAndCourseId(Long learnerId, Long courseId);
}
