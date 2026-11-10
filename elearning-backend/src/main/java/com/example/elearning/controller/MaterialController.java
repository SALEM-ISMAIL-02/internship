package com.example.elearning.controller;

import com.example.elearning.entity.Material;
import com.example.elearning.service.MaterialQueryService;
import com.example.elearning.service.MaterialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;
    private final MaterialQueryService materialQueryService;

    @PostMapping(value = "/upload/{courseId}", consumes = "multipart/form-data")
    public Material upload(@PathVariable Long courseId, @RequestParam("file") MultipartFile file)
            throws IOException {
        return materialService.upload(courseId, file);
    }

    @GetMapping("/course/{courseId}")
    public List<Material> byCourse(@PathVariable Long courseId) {
        return materialQueryService.byCourse(courseId);
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<?> file(@PathVariable Long id) {
        try {
            Material material = materialQueryService.get(id);
            Path path = Paths.get(material.getFilePath());
            Resource resource = new FileSystemResource(path);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(path);
            MediaType mediaType = contentType == null
                    ? MediaType.APPLICATION_OCTET_STREAM : MediaType.parseMediaType(contentType);
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + material.getOriginalName() + "\"")
                    .body(resource);
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            materialService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException | IOException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
