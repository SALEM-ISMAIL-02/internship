package com.example.elearning.extract;

import com.example.elearning.entity.Material;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class DocxTextExtractor implements TextExtractor {

    @Override
    public String extract(Path path) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(Files.newInputStream(path));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    @Override
    public Material.MaterialType supportedType() {
        return Material.MaterialType.DOC;
    }
}
