package com.example.elearning.service;

import com.example.elearning.entity.*;
import com.example.elearning.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgressService {

    private final LearnerProgressRepository progressRepository;
    private final CourseRepository courseRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuizRepository quizRepository;

    public LearnerProgress updateProgress(Long learnerId, Long courseId, int progress) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        LearnerProgress lp = progressRepository.findByLearnerIdAndCourseId(learnerId, courseId)
                .orElseGet(() -> LearnerProgress.builder()
                        .learner(User.builder().id(learnerId).build())
                        .course(course)
                        .progress(0)
                        .completed(false)
                        .build());
        lp.setProgress(Math.max(0, Math.min(100, progress)));
        lp.setCompleted(lp.getProgress() >= 100);
        return progressRepository.save(lp);
    }

    public List<LearnerProgress> getProgress(Long learnerId) {
        return progressRepository.findByLearnerId(learnerId);
    }

    public QuizResult submitQuiz(Long learnerId, Long quizId, Map<Long, Integer> answers) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        int total = quiz.getQuestions().size();
        if (total == 0) throw new IllegalStateException("Quiz has no questions");

        long correct = quiz.getQuestions().stream()
                .filter(q -> answers != null && answers.get(q.getId()) != null
                        && answers.get(q.getId()) == q.getCorrectAnswer())
                .count();
        int score = (int) Math.round(correct * 100.0 / total);

        QuizResult result = QuizResult.builder()
                .learner(User.builder().id(learnerId).build())
                .quiz(quiz)
                .score(score)
                .date(LocalDateTime.now())
                .build();
        result = quizResultRepository.save(result);

        // Simple rule: passing the quiz bumps course progress by 25%
        int bonus = score >= 50 ? 25 : 0;
        LearnerProgress lp = progressRepository.findByLearnerIdAndCourseId(learnerId, quiz.getCourse().getId())
                .orElseGet(() -> LearnerProgress.builder()
                        .learner(User.builder().id(learnerId).build())
                        .course(quiz.getCourse())
                        .progress(0)
                        .completed(false)
                        .build());
        lp.setProgress(Math.min(100, lp.getProgress() + bonus));
        lp.setCompleted(lp.getProgress() >= 100);
        progressRepository.save(lp);
        return result;
    }

    public List<QuizResult> getResults(Long learnerId) {
        return quizResultRepository.findByLearnerId(learnerId);
    }
}
