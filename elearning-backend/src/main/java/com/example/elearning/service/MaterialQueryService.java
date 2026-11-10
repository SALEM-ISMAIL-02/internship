package com.example.elearning.service;

import com.example.elearning.audit.AuditEvent;
import com.example.elearning.entity.Material;
import com.example.elearning.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SRP: read-side for materials (DIP: controllers never touch repositories).
 */
@Service
@RequiredArgsConstructor
public class MaterialQueryService {

    private final MaterialRepository materialRepository;

    @Transactional(readOnly = true)
    public List<Material> byCourse(Long courseId) {
        return materialRepository.findByCourseId(courseId);
    }

    public Material get(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Material not found"));
    }
}
