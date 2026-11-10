package com.example.elearning.extract;

import com.example.elearning.entity.Material;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/** Test-only extractor (OCP proof): adds OTHER support without touching prod code. */
@Component
@Profile("test")
public class OtherTestTextExtractor implements TextExtractor {

    @Override
    public String extract(Path path) {
        return "test source text";
    }

    @Override
    public Material.MaterialType supportedType() {
        return Material.MaterialType.OTHER;
    }
}
