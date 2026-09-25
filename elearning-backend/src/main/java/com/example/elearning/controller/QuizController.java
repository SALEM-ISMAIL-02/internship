package com.example.elearning.controller;

import com.example.elearning.entity.Quiz;
import com.example.elearning.repository.QuizRepository;
import com.example.elearning.service.QuizGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizGenerationService quizGenerationService;
    private final QuizRepository quizRepository;

    /** AI-generates a quiz from a course material. Returns an unpublished quiz for instructor review. */
    @PostMapping("/generate")
    public Quiz generate(@RequestBody Map<String, Object> body) throws Exception {
        Long materialId = Long.valueOf(body.get("materialId").toString());
        int num = Integer.parseInt(body.getOrDefault("numQuestions", 5).toString());
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long creatorId = Long.valueOf(body.getOrDefault("creatorId", 0).toString());
        return quizGenerationService.generateQuiz(materialId, num, creatorId);
    }

    @GetMapping("/course/{courseId}")
    public List<Quiz> byCourse(@PathVariable Long courseId) {
        return quizRepository.findByCourseId(courseId);
    }

    /** Instructor review/edit before publishing. */
    @PutMapping("/{id}/publish")
    public Quiz publish(@PathVariable Long id, @RequestBody(required = false) Quiz updated) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        if (updated != null && updated.getQuestions() != null) {
            quiz.setQuestions(updated.getQuestions());
            quiz.getQuestions().forEach(q -> q.setQuiz(quiz));
        }
        quiz.setPublished(true);
        return quizRepository.save(quiz);
    }

    /** Learner fetches a published quiz to take (answers hidden). */
    @GetMapping("/{id}/take")
    public Quiz take(@PathVariable Long id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        quiz.getQuestions().forEach(q -> q.setCorrectAnswer(-1));
        return quiz;
    }
}
