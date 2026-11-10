package com.example.elearning.controller;

import com.example.elearning.entity.Quiz;
import com.example.elearning.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Thin HTTP adapter (SRP): request parsing + status codes only.
 * All quiz business rules live in {@link QuizService}.
 */
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final ObjectMapper objectMapper;
    private final HttpServletRequest request;

    /** AI-generates a quiz from a course material. Returns an unpublished draft for review. */
    @PostMapping("/generate")
    public ResponseEntity<?> generate(@RequestBody Map<String, Object> body,
                                      @RequestHeader(value = "X-User-Email", required = false) String email) {
        try {
            Long materialId = Long.valueOf(body.get("materialId").toString());
            int num = Integer.parseInt(body.getOrDefault("numQuestions", 5).toString());
            Long creatorId = Long.valueOf(body.getOrDefault("creatorId", 0).toString());
            return ResponseEntity.ok(quizService.generate(materialId, num, creatorId, email));
        } catch (IllegalArgumentException | IllegalStateException | java.io.IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/manual")
    public ResponseEntity<?> manual(@RequestBody Map<String, Object> body,
                                    @RequestHeader(value = "X-User-Email", required = false) String email) {
        try {
            Long courseId = Long.valueOf(body.get("courseId").toString());
            Long creatorId = Long.valueOf(body.getOrDefault("creatorId", 0).toString());
            return ResponseEntity.ok(quizService.createManual(courseId, creatorId, email));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/course/{courseId}")
    public List<Quiz> byCourse(@PathVariable Long courseId,
                               @RequestHeader(value = "X-User-Role", required = false) String role) {
        boolean learner = "LEARNER".equalsIgnoreCase(role);
        return learner ? quizService.publishedTakeableByCourse(courseId) : quizService.byCourse(courseId);
    }

    @GetMapping("/results")
    public List<Map<String, Object>> reviewResults(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        boolean admin = "ADMIN".equalsIgnoreCase(role);
        Long instructorId = admin ? null : null;
        if (!admin) {
            instructorId = email == null ? null : quizService.findUserId(email);
        }
        return quizService.reviewResults(instructorId).stream().map(this::reviewPayload).toList();
    }

    @PutMapping("/results/{id}/approve")
    public com.example.elearning.entity.QuizResult approveResult(@PathVariable Long id) {
        return quizService.approveResult(id);
    }

    @PutMapping("/results/{id}/unapprove")
    public com.example.elearning.entity.QuizResult unapproveResult(@PathVariable Long id) {
        return quizService.unapproveResult(id);
    }

    private Map<String, Object> reviewPayload(com.example.elearning.entity.QuizResult result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", result.getId());
        payload.put("learner", Map.of(
                "id", result.getLearner().getId(),
                "name", result.getLearner().getName(),
                "email", result.getLearner().getEmail()));
        payload.put("quiz", Map.of(
                "id", result.getQuiz().getId(),
                "courseId", result.getQuiz().getCourse().getId(),
                "courseTitle", result.getQuiz().getCourse().getTitle()));
        payload.put("score", result.getScore());
        payload.put("date", result.getDate());
        payload.put("approved", result.isApproved());
        try {
            var storedAnswers = objectMapper.readTree(
                    result.getAnswersJson() == null ? "{}" : result.getAnswersJson());
            var normalizedAnswers = objectMapper.createObjectNode();
            var questions = result.getQuiz().getQuestions();
            for (int index = 0; index < questions.size(); index++) {
                var question = questions.get(index);
                var answer = question.getId() == null
                        ? null
                        : storedAnswers.get(String.valueOf(question.getId()));
                if (answer == null) {
                    answer = storedAnswers.get(String.valueOf(index));
                }
                if (answer != null) {
                    normalizedAnswers.set(String.valueOf(question.getId()), answer);
                }
            }
            payload.put("answers", normalizedAnswers);
        } catch (Exception e) {
            throw new IllegalStateException("Stored quiz answers are invalid", e);
        }
        payload.put("questions", result.getQuiz().getQuestions());
        return payload;
    }

    /** Instructor review/edit + CHAIN OF RESPONSIBILITY validation, then STATE transition to published. */
    @PutMapping("/{id}/publish")
    public ResponseEntity<?> publish(@PathVariable Long id, @RequestBody(required = false) Quiz updated) {
        try {
            return ResponseEntity.ok(quizService.publish(id, updated, actor()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Instructor question editor: saves draft edits only (STATE rule - a published quiz must be
     * unpublished first, and validation still happens when it is re-published).
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Quiz updated) {
        try {
            return ResponseEntity.ok(quizService.updateDraft(id, updated, actor()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Move a published quiz back to draft (STATE transition). */
    @PutMapping("/{id}/unpublish")
    public Quiz unpublish(@PathVariable Long id) {
        return quizService.unpublish(id, actor());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            quizService.delete(id, actor());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** PROTOTYPE: clone this quiz (questions included) into a new draft, optionally for another course. */
    @PostMapping("/{id}/duplicate")
    public Quiz duplicate(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        Long targetCourseId = body == null || body.get("courseId") == null
                ? null : Long.valueOf(body.get("courseId").toString());
        return quizService.duplicate(id, targetCourseId, actor());
    }

    /** Learner fetches a published quiz to take (answers hidden). */
    @GetMapping("/{id}/take")
    public ResponseEntity<?> take(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(quizService.takeableCopy(id));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private String actor() {
        return request.getHeader("X-User-Email");
    }
}
