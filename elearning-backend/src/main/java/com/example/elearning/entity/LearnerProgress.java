package com.example.elearning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "learner_progress",
       uniqueConstraints = @UniqueConstraint(columnNames = {"learner_id", "course_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearnerProgress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "learner_id")
    private User learner;

    @ManyToOne(optional = false) @JoinColumn(name = "course_id")
    private Course course;

    @Column(nullable = false)
    private int progress; // 0-100

    @Column(nullable = false)
    private boolean completed;
}
