package com.example.elearning.extract;

import com.example.elearning.entity.Material;

import java.io.IOException;
import java.nio.file.Path;

/** STRATEGY: one algorithm per source-document format. */
public interface TextExtractor {

    /** @return the plain text of the document, or an empty string when it holds no text. */
    String extract(Path path) throws IOException;

    /** The material type this strategy handles. */
    Material.MaterialType supportedType();
}
