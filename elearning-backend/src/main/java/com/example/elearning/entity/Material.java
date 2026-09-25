package com.example.elearning.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "materials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Material {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaterialType type;

    @Column(nullable = false)
    private String filePath;

    private String originalName;

    @ManyToOne(optional = false) @JoinColumn(name = "course_id")
    private Course course;

    public enum MaterialType { PDF, PPT, DOC, VIDEO, OTHER }
}
