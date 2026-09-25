package com.example.elearning.service;

import com.example.elearning.entity.Course;
import com.example.elearning.entity.Material;
import com.example.elearning.repository.CourseRepository;
import com.example.elearning.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final CourseRepository courseRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public Material upload(Long courseId, MultipartFile file) throws IOException {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        Files.createDirectories(Paths.get(uploadDir));
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path target = Paths.get(uploadDir, fileName);
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        Material material = Material.builder()
                .type(detectType(file.getOriginalFilename()))
                .filePath(target.toString())
                .originalName(file.getOriginalFilename())
                .course(course)
                .build();
        return materialRepository.save(material);
    }

    /** Extracts plain text from PDF / DOCX / PPTX files for AI quiz generation. */
    public String extractText(Material material) throws IOException {
        Path path = Paths.get(material.getFilePath());
        return switch (material.getType()) {
            case PDF -> extractPdf(path);
            case DOC -> extractDocx(path);
            case PPT -> extractPptx(path);
            default -> "";
        };
    }

    private String extractPdf(Path path) throws IOException {
        try (PDDocument doc = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String extractDocx(Path path) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(Files.newInputStream(path));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    private String extractPptx(Path path) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(Files.newInputStream(path))) {
            StringBuilder sb = new StringBuilder();
            ppt.getSlides().forEach(slide ->
                slide.getShapes().forEach(shape -> {
                    if (shape instanceof org.apache.poi.xslf.usermodel.XSLFTextShape ts) {
                        sb.append(ts.getText()).append('\n');
                    }
                }));
            return sb.toString();
        }
    }

    private Material.MaterialType detectType(String name) {
        String n = name == null ? "" : name.toLowerCase();
        if (n.endsWith(".pdf")) return Material.MaterialType.PDF;
        if (n.endsWith(".doc") || n.endsWith(".docx")) return Material.MaterialType.DOC;
        if (n.endsWith(".ppt") || n.endsWith(".pptx")) return Material.MaterialType.PPT;
        if (n.endsWith(".mp4") || n.endsWith(".avi") || n.endsWith(".mov")) return Material.MaterialType.VIDEO;
        return Material.MaterialType.OTHER;
    }
}
