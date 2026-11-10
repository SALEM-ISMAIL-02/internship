package com.example.elearning.service;

import com.example.elearning.audit.AuditEvent;
import com.example.elearning.entity.*;
import com.example.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

/**
 * SRP: ONLY tracks course progress. Quiz grading lives in {@link QuizGradingService}.
 */
@Service
@RequiredArgsConstructor
public class ProgressService {

    private final LearnerProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizGradingService quizGradingService;
    private final ApplicationEventPublisher eventPublisher;

    public List<LearnerProgress> getProgress(Long learnerId) {
        return progressRepository.findByLearnerId(learnerId);
    }

    /**
     * Delegates scoring to {@link QuizGradingService}, then applies the
     * progress-bonus policy (SRP: this class owns the bonus rule only).
     */
    @Transactional
    public QuizResult submitQuiz(Long learnerId, Long quizId, Map<Long, Integer> answers) {
        if (quizResultRepository.findByLearnerId(learnerId).stream()
                .anyMatch(result -> result.getQuiz().getId().equals(quizId))) {
            throw new IllegalStateException("This quiz has already been submitted and cannot be retaken");
        }
        QuizGradingService.GradingOutcome outcome = quizGradingService.grade(learnerId, quizId, answers);
        int score = outcome.result().getScore();

        LearnerProgress lp = findOrInit(learnerId, outcome.quiz().getCourse());
        lp.setProgress(100);
        lp.setCompleted(false);
        progressRepository.save(lp);

        eventPublisher.publishEvent(new AuditEvent("QUIZ_SUBMITTED", null, "QUIZ", quizId,
                "learner #" + learnerId + " scored " + score + "%"));
        return outcome.result();
    }

    public List<QuizResult> getResults(Long learnerId) {
        return quizResultRepository.findByLearnerId(learnerId);
    }

    private LearnerProgress findOrInit(Long learnerId, Course course) {
        return progressRepository.findByLearnerIdAndCourseId(learnerId, course.getId())
                .orElseGet(() -> LearnerProgress.builder()
                        .learner(User.builder().id(learnerId).build())
                        .course(course)
                        .progress(0)
                        .completed(false)
                        .build());
    }
}
