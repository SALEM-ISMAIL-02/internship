package com.example.elearning.service;

import com.example.elearning.audit.AuditEvent;
import com.example.elearning.entity.Course;
import com.example.elearning.entity.User;
import com.example.elearning.repository.CourseRepository;
import com.example.elearning.repository.UserRepository;
import com.example.elearning.repository.MaterialRepository;
import com.example.elearning.repository.QuizRepository;
import com.example.elearning.repository.QuizResultRepository;
import com.example.elearning.repository.LearnerProgressRepository;
import com.example.elearning.state.CourseLifecycle;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SRP: owns the Course aggregate (CRUD + lifecycle).
 * Also owns instructor resolution (DIP: controllers no longer touch UserRepository).
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final CourseLifecycle courseLifecycle;
    private final ApplicationEventPublisher eventPublisher;
    private final MaterialRepository materialRepository;
    private final QuizRepository quizRepository;
    private final QuizResultRepository quizResultRepository;
    private final LearnerProgressRepository progressRepository;

    public List<Course> list() {
        return courseRepository.findAll();
    }

    public Course get(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
    }

    public Course create(Course course, String instructorEmail) {
        course.setId(null);
        if (instructorEmail != null) {
            userRepository.findByEmail(instructorEmail).ifPresent(course::setInstructor);
        }
        if (course.getStatus() == null) {
            course.setStatus(Course.Status.DRAFT);
        }
        Course saved = courseRepository.save(course);
        eventPublisher.publishEvent(new AuditEvent("COURSE_CREATED", instructorEmail, "COURSE", saved.getId(),
                saved.getTitle() + " [" + saved.getPriority() + "/" + saved.getStatus() + "]"));
        return saved;
    }

    public Course update(Long id, Course updated) {
        Course existing = get(id);
        Course.Status previous = existing.getStatus();
        if (updated.getStatus() != null && updated.getStatus() != previous) {
            courseLifecycle.assertTransition(previous, updated.getStatus());
        }
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setPriority(updated.getPriority());
        existing.setStatus(updated.getStatus() != null ? updated.getStatus() : previous);
        Course saved = courseRepository.save(existing);

        if (previous != saved.getStatus()) {
            eventPublisher.publishEvent(new AuditEvent("COURSE_STATUS_CHANGED", null, "COURSE", id,
                    previous + " -> " + saved.getStatus()));
        } else {
            eventPublisher.publishEvent(new AuditEvent("COURSE_UPDATED", null, "COURSE", id, saved.getTitle()));
        }
        return saved;
    }

    /** Explicit lifecycle endpoint: DRAFT -> PUBLISHED -> ARCHIVED -> DRAFT. */
    public Course changeStatus(Long id, Course.Status target) {
        Course course = get(id);
        Course.Status previous = course.getStatus() == null ? Course.Status.DRAFT : course.getStatus();
        courseLifecycle.assertTransition(previous, target);
        course.setStatus(target);
        Course saved = courseRepository.save(course);
        eventPublisher.publishEvent(new AuditEvent("COURSE_STATUS_CHANGED", null, "COURSE", id,
                previous + " -> " + target));
        return saved;
    }

    @Transactional
    public void delete(Long id) {
        Course course = get(id);
        quizResultRepository.deleteByQuizCourseId(id);
        progressRepository.deleteByCourseId(id);
        quizRepository.deleteByCourseId(id);
        materialRepository.deleteByCourseId(id);
        courseRepository.deleteById(id);
        eventPublisher.publishEvent(new AuditEvent("COURSE_DELETED", null, "COURSE", id, course.getTitle()));
    }
}
