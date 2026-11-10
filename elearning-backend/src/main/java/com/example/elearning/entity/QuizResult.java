package com.example.elearning.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_results")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "learner_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User learner;

    @ManyToOne(optional = false) @JoinColumn(name = "quiz_id")
    private Quiz quiz;

    @Column(nullable = false)
    private int score; // percentage

    @Column(nullable = false)
    private LocalDateTime date;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String answersJson;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean approved;
}
