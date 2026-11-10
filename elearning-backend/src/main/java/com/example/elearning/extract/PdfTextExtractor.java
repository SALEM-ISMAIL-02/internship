package com.example.elearning.extract;

import com.example.elearning.entity.Material;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class PdfTextExtractor implements TextExtractor {

    @Override
    public String extract(Path path) throws IOException {
        try (PDDocument doc = Loader.loadPDF(path.toFile())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    @Override
    public Material.MaterialType supportedType() {
        return Material.MaterialType.PDF;
    }
}
