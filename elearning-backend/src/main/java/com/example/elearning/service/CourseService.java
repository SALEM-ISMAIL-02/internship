package com.example.elearning.service;

import com.example.elearning.entity.Course;
import com.example.elearning.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    public List<Course> list() {
        return courseRepository.findAll();
    }

    public Course get(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + id));
    }

    public Course create(Course course, String instructorEmail) {
        course.setId(null);
        return courseRepository.save(course);
    }

    public Course update(Long id, Course updated) {
        Course existing = get(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setCategory(updated.getCategory());
        existing.setPriority(updated.getPriority());
        existing.setStatus(updated.getStatus());
        return courseRepository.save(existing);
    }

    public void delete(Long id) {
        courseRepository.deleteById(id);
    }
}
