package com.example.elearning.service;

import com.example.elearning.audit.AuditEvent;
import com.example.elearning.entity.Course;
import com.example.elearning.entity.Material;
import com.example.elearning.extract.TextExtractorFactory;
import com.example.elearning.repository.CourseRepository;
import com.example.elearning.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

/**
 * SRP: orchestrates material use-cases only.
 * Filesystem bytes -&gt; {@link FileStorageService}, filename typing -&gt; {@link FileTypeResolver},
 * text decoding -&gt; {@link TextExtractorFactory} strategy.
 */
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final CourseRepository courseRepository;
    private final TextExtractorFactory textExtractorFactory;
    private final FileStorageService fileStorageService;
    private final FileTypeResolver fileTypeResolver;
    private final ApplicationEventPublisher eventPublisher;

    public Material upload(Long courseId, MultipartFile file) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        Path target = fileStorageService.store(file);

        Material material = Material.builder()
                .type(fileTypeResolver.resolve(file.getOriginalFilename()))
                .filePath(target.toString())
                .originalName(file.getOriginalFilename())
                .course(course)
                .build();
        Material saved = materialRepository.save(material);

        eventPublisher.publishEvent(new AuditEvent(
                "MATERIAL_UPLOADED", null, "MATERIAL", saved.getId(),
                saved.getOriginalName() + " (" + saved.getType() + ") -> course #" + courseId));
        return saved;
    }
    /**
     * Extracts plain text for AI quiz generation by delegating to the strategy that
     * handles the material's format (Factory + Strategy).
     */
    public String extractText(Material material) throws IOException {
        return textExtractorFactory.forType(material.getType())
                .extract(Paths.get(material.getFilePath()));
    }

    public void delete(Long id) throws IOException {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));
        materialRepository.delete(material);
        Files.deleteIfExists(Paths.get(material.getFilePath()));
        eventPublisher.publishEvent(new AuditEvent(
                "MATERIAL_DELETED", null, "MATERIAL", id, material.getOriginalName()));
    }
}
