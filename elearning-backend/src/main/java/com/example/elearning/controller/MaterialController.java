package com.example.elearning.controller;

import com.example.elearning.entity.Material;
import com.example.elearning.repository.MaterialRepository;
import com.example.elearning.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialRepository materialRepository;

    @PostMapping(value = "/upload/{courseId}", consumes = "multipart/form-data")
    public Material upload(@PathVariable Long courseId, @RequestParam("file") MultipartFile file)
            throws IOException {
        return materialService.upload(courseId, file);
    }

    @GetMapping("/course/{courseId}")
    public List<Material> byCourse(@PathVariable Long courseId) {
        return materialRepository.findByCourseId(courseId);
    }
}
