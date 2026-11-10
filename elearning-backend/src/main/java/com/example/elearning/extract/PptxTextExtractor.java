package com.example.elearning.extract;

import com.example.elearning.entity.Material;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class PptxTextExtractor implements TextExtractor {

    @Override
    public String extract(Path path) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(Files.newInputStream(path))) {
            StringBuilder sb = new StringBuilder();
            ppt.getSlides().forEach(slide ->
                    slide.getShapes().forEach(shape -> {
                        if (shape instanceof XSLFTextShape textShape) {
                            sb.append(textShape.getText()).append('\n');
                        }
                    }));
            return sb.toString();
        }
    }

    @Override
    public Material.MaterialType supportedType() {
        return Material.MaterialType.PPT;
    }
}
