package com.example.elearning.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "learner_id")
    private User learner;

    @ManyToOne(optional = false) @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(nullable = false)
    private int score; // percentage

    @Column(nullable = false)
    private LocalDateTime date;
}
