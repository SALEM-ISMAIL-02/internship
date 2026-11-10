package com.example.elearning.controller;

import com.example.elearning.entity.LearnerProgress;
import com.example.elearning.entity.QuizResult;
import com.example.elearning.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping
    public List<LearnerProgress> myProgress(@RequestParam Long learnerId) {
        return progressService.getProgress(learnerId);
    }

    @PostMapping("/quiz/{quizId}/submit")
    public Map<String, Object> submit(@PathVariable Long quizId, @RequestParam Long learnerId,
                                      @RequestBody Map<String, Object> body) {
        try {
            Map<Long, Integer> answers = new HashMap<>();
            body.forEach((k, v) -> answers.put(Long.valueOf(k), ((Number) v).intValue()));
            QuizResult result = progressService.submitQuiz(learnerId, quizId, answers);
            return Map.of("id", result.getId(), "date", result.getDate(),
                    "approved", result.isApproved(), "status", "SUBMITTED");
        } catch (IllegalArgumentException | IllegalStateException e) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    @GetMapping("/results")
    public List<Map<String, Object>> results(@RequestParam Long learnerId) {
        return progressService.getResults(learnerId).stream().map(result -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", result.getId());
            payload.put("date", result.getDate());
            payload.put("approved", result.isApproved());
            payload.put("quiz", Map.of(
                    "id", result.getQuiz().getId(),
                    "courseId", result.getQuiz().getCourse().getId(),
                    "courseTitle", result.getQuiz().getCourse().getTitle()));
            return payload;
        }).toList();
    }
}
