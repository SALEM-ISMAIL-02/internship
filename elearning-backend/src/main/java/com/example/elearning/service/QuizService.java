package com.example.elearning.service;

import com.example.elearning.audit.AuditEvent;
import com.example.elearning.entity.Course;
import com.example.elearning.entity.Question;
import com.example.elearning.entity.Quiz;
import com.example.elearning.quiz.QuizCloner;
import com.example.elearning.quiz.QuizGenerationRequest;
import com.example.elearning.quiz.QuizGenerator;
import com.example.elearning.repository.CourseRepository;
import com.example.elearning.repository.QuizRepository;
import com.example.elearning.repository.QuizResultRepository;
import com.example.elearning.repository.LearnerProgressRepository;
import com.example.elearning.repository.UserRepository;
import com.example.elearning.state.QuizLifecycle;
import com.example.elearning.validation.QuizValidationChain;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.example.elearning.entity.User;

/**
 * SRP: owns the Quiz aggregate (generate / review / publish / duplicate / take).
 * The controller is a thin HTTP adapter: parsing + status codes only.
 * DIP: depends on abstractions (QuizGenerator) and domain collaborators.
 */
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizGenerator quizGenerator;
    private final QuizRepository quizRepository;
    private final CourseRepository courseRepository;
    private final QuizValidationChain validationChain;
    private final QuizLifecycle quizLifecycle;
    private final QuizCloner quizCloner;
    private final ApplicationEventPublisher eventPublisher;
    private final QuizResultRepository quizResultRepository;
    private final LearnerProgressRepository progressRepository;
    private final UserRepository userRepository;

    public Quiz generate(Long materialId, int numQuestions, Long creatorId, String actor) throws IOException {
        Quiz quiz = quizGenerator.generate(new QuizGenerationRequest(materialId, numQuestions, creatorId));
        eventPublisher.publishEvent(new AuditEvent("QUIZ_GENERATED", actor, "QUIZ", quiz.getId(),
                quiz.getQuestions().size() + " questions from material #" + materialId));
        return quiz;
    }

    @Transactional
    public Quiz createManual(Long courseId, Long creatorId, String actor) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        User creator = creatorId == null || creatorId == 0 ? null
                : userRepository.findById(creatorId).orElse(null);
        Quiz quiz = Quiz.builder().course(course).createdBy(creator)
                .aiGenerated(false).published(false).questions(new ArrayList<>()).build();
        Quiz saved = quizRepository.save(quiz);
        eventPublisher.publishEvent(new AuditEvent("QUIZ_CREATED_MANUALLY", actor, "QUIZ", saved.getId()));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Quiz> byCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    public List<Quiz> publishedTakeableByCourse(Long courseId) {
        return quizRepository.findByCourseId(courseId).stream()
                .filter(Quiz::isPublished)
                .map(quiz -> takeableCopy(quiz.getId()))
                .toList();
    }

    @Transactional
    public Quiz publish(Long id, Quiz updated, String actor) {
        Quiz quiz = find(id);
        if (updated != null && updated.getQuestions() != null) {
            replaceQuestions(quiz, updated.getQuestions());
        }
        validationChain.validateOrThrow(quiz);
        quizLifecycle.publish(quiz);
        Quiz saved = quizRepository.save(quiz);
        eventPublisher.publishEvent(new AuditEvent("QUIZ_PUBLISHED", actor, "QUIZ", id,
                saved.getQuestions().size() + " questions validated"));
        return saved;
    }

    @Transactional
    public Quiz updateDraft(Long id, Quiz updated, String actor) {
        Quiz quiz = find(id);
        if (quiz.isPublished()) {
            throw new IllegalStateException("Quiz #" + id + " is published - unpublish it before editing.");
        }
        if (updated != null && updated.getQuestions() != null) {
            replaceQuestions(quiz, updated.getQuestions());
        }
        Quiz saved = quizRepository.save(quiz);
        eventPublisher.publishEvent(new AuditEvent("QUIZ_UPDATED", actor, "QUIZ", id,
                saved.getQuestions().size() + " questions saved to draft"));
        return saved;
    }

    @Transactional
    public Quiz unpublish(Long id, String actor) {
        Quiz quiz = find(id);
        quizLifecycle.unpublish(quiz);
        Quiz saved = quizRepository.save(quiz);
        eventPublisher.publishEvent(new AuditEvent("QUIZ_UNPUBLISHED", actor, "QUIZ", id));
        return saved;
    }

    @Transactional(readOnly = true)
    public List<com.example.elearning.entity.QuizResult> reviewResults(Long instructorId) {
        return instructorId == null
                ? quizResultRepository.findAll()
                : quizResultRepository.findByQuizCourseInstructorId(instructorId);
    }

    public Long findUserId(String email) {
        return userRepository.findByEmail(email)
                .map(com.example.elearning.entity.User::getId)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
    }

    @Transactional
    public com.example.elearning.entity.QuizResult approveResult(Long resultId) {
        var result = quizResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz result not found"));
        result.setApproved(true);
        var course = result.getQuiz().getCourse();
        progressRepository.findByLearnerIdAndCourseId(result.getLearner().getId(), course.getId())
                .ifPresent(progress -> {
                    progress.setProgress(100);
                    progress.setCompleted(true);
                    progressRepository.save(progress);
                });
        return quizResultRepository.save(result);
    }

    @Transactional
    public com.example.elearning.entity.QuizResult unapproveResult(Long resultId) {
        var result = quizResultRepository.findById(resultId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz result not found"));
        result.setApproved(false);
        progressRepository.findByLearnerIdAndCourseId(
                result.getLearner().getId(), result.getQuiz().getCourse().getId())
                .ifPresent(progress -> {
                    progress.setProgress(100);
                    progress.setCompleted(false);
                    progressRepository.save(progress);
                });
        return quizResultRepository.save(result);
    }

    @Transactional
    public Quiz duplicate(Long id, Long targetCourseId, String actor) {
        Quiz source = find(id);
        Course target = targetCourseId == null ? null
                : courseRepository.findById(targetCourseId).orElse(null);
        Quiz saved = quizRepository.save(quizCloner.clone(source, target, null));
        eventPublisher.publishEvent(new AuditEvent("QUIZ_DUPLICATED", actor, "QUIZ", saved.getId(),
                "cloned from quiz #" + id + " (" + saved.getQuestions().size() + " questions)"));
        return saved;
    }

    @Transactional
    public void delete(Long id, String actor) {
        Quiz quiz = find(id);
        quizResultRepository.deleteByQuizId(id);
        quizRepository.delete(quiz);
        eventPublisher.publishEvent(new AuditEvent("QUIZ_DELETED", actor, "QUIZ", id));
    }

    /**
     * Returns a detached take-home copy with answers hidden.
     * Never mutates the managed entity (hiding answers on it would dirty-check to -1).
     */
    @Transactional(readOnly = true)
    public Quiz takeableCopy(Long id) {
        Quiz quiz = find(id);
        quizLifecycle.assertTakeable(quiz);
        Quiz copy = Quiz.builder()
                .id(quiz.getId())
                .course(quiz.getCourse())
                .createdBy(quiz.getCreatedBy())
                .aiGenerated(quiz.isAiGenerated())
                .published(quiz.isPublished())
                .questions(new ArrayList<>())
                .build();
        for (Question q : quiz.getQuestions()) {
            copy.getQuestions().add(Question.builder()
                    .id(q.getId())
                    .text(q.getText())
                    .options(q.getOptions() == null ? List.of() : List.copyOf(q.getOptions()))
                    .correctAnswer(-1)
                    .build());
        }
        return copy;
    }

    private Quiz find(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
    }

    private void replaceQuestions(Quiz quiz, List<Question> incoming) {
        Map<Long, Question> existingById = new HashMap<>();
        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                if (q.getId() != null) {
                    existingById.put(q.getId(), q);
                }
            }
        } else {
            quiz.setQuestions(new ArrayList<>());
        }
        List<Question> resolved = new ArrayList<>();
        for (Question q : incoming) {
            Question managed = q.getId() != null ? existingById.get(q.getId()) : null;
            if (managed != null) {
                managed.setText(q.getText());
                managed.setOptions(q.getOptions());
                managed.setCorrectAnswer(q.getCorrectAnswer());
                resolved.add(managed);
            } else {
                q.setId(null);
                q.setQuiz(quiz);
                resolved.add(q);
            }
        }
        quiz.getQuestions().clear();
        resolved.forEach(quiz.getQuestions()::add);
    }
}
