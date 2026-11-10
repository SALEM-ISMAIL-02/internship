package com.example.elearning.controller;

import com.example.elearning.entity.Course;
import com.example.elearning.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public List<Course> list() {
        return courseService.list();
    }

    @GetMapping("/{id}")
    public Course get(@PathVariable Long id) {
        return courseService.get(id);
    }

    @PostMapping
    public Course create(@RequestBody Course course, @RequestHeader(value = "X-User-Email", required = false) String email) {
        return courseService.create(course, email);
    }

    @PutMapping("/{id}")
    public Course update(@PathVariable Long id, @RequestBody Course course) {
        return courseService.update(id, course);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            courseService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "error", "Module cannot be deleted because related records still exist."));
        }
    }

    /** Explicit lifecycle transition endpoint (State pattern rules are enforced in the service). */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            Course.Status target = Course.Status.valueOf(body.getOrDefault("status", "").toUpperCase());
            return ResponseEntity.ok(courseService.changeStatus(id, target));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
