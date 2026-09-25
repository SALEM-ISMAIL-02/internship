package com.example.elearning.controller;

import com.example.elearning.entity.LearnerProgress;
import com.example.elearning.entity.QuizResult;
import com.example.elearning.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @PutMapping
    @PreAuthorize("hasRole('LEARNER')")
    public LearnerProgress update(Principal principal, @RequestBody Map<String, Object> body) {
        Long courseId = Long.valueOf(body.get("courseId").toString());
        int progress = Integer.parseInt(body.getOrDefault("progress", 0).toString());
        // The JWT subject is the email; learnerId is supplied by client as a convenience here.
        Long learnerId = Long.valueOf(body.get("learnerId").toString());
        return progressService.updateProgress(learnerId, courseId, progress);
    }

    @GetMapping
    @PreAuthorize("hasRole('LEARNER')")
    public List<LearnerProgress> myProgress(@RequestParam Long learnerId) {
        return progressService.getProgress(learnerId);
    }

    @PostMapping("/quiz/{quizId}/submit")
    @PreAuthorize("hasRole('LEARNER')")
    public QuizResult submit(@PathVariable Long quizId, @RequestParam Long learnerId,
                             @RequestBody Map<String, Integer> answers) {
        return progressService.submitQuiz(learnerId, quizId, answers);
    }

    @GetMapping("/results")
    @PreAuthorize("hasRole('LEARNER')")
    public List<QuizResult> results(@RequestParam Long learnerId) {
        return progressService.getResults(learnerId);
    }
}
