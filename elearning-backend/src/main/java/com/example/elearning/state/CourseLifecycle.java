package com.example.elearning.state;

import com.example.elearning.entity.Course;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * STATE: the course lifecycle is a small state machine. Only the transitions declared here
 * are legal, which stops nonsense such as archiving straight into "published" or
 * silently rewinding a published mandatory module to DRAFT.
 */
@Component
public class CourseLifecycle {

    private static final Map<Course.Status, Set<Course.Status>> ALLOWED = new EnumMap<>(Course.Status.class);

    static {
        ALLOWED.put(Course.Status.DRAFT, EnumSet.of(Course.Status.PUBLISHED, Course.Status.ARCHIVED));
        ALLOWED.put(Course.Status.PUBLISHED, EnumSet.of(Course.Status.ARCHIVED));
        ALLOWED.put(Course.Status.ARCHIVED, EnumSet.of(Course.Status.DRAFT));
    }

    public void assertTransition(Course.Status from, Course.Status to) {
        if (from == null || to == null || from == to) {
            return;
        }
        if (!allowedTargets(from).contains(to)) {
            throw new IllegalStateException(
                    "Illegal course status transition: " + from + " -> " + to
                            + ". Allowed from " + from + ": " + allowedTargets(from));
        }
    }

    public Set<Course.Status> allowedTargets(Course.Status from) {
        return ALLOWED.getOrDefault(from, EnumSet.noneOf(Course.Status.class));
    }

    public Course.Status nextStatus(Course course) {
        return switch (course.getStatus() == null ? Course.Status.DRAFT : course.getStatus()) {
            case DRAFT -> Course.Status.PUBLISHED;
            case PUBLISHED -> Course.Status.ARCHIVED;
            case ARCHIVED -> Course.Status.DRAFT;
        };
    }
}
