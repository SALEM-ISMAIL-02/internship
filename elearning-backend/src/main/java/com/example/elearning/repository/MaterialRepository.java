package com.example.elearning.repository;

import com.example.elearning.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByCourseId(Long courseId);
    void deleteByCourseId(Long courseId);
}
