package com.example.elearning.extract;

import com.example.elearning.entity.Material;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * FACTORY: resolves the right {@link TextExtractor} for a {@link Material.MaterialType}.
 * Strategies register themselves (Spring injects every TextExtractor bean), so adding a
 * new format means adding one class and no edits here.
 */
@Component
public class TextExtractorFactory {

    private final Map<Material.MaterialType, TextExtractor> extractors =
            new EnumMap<>(Material.MaterialType.class);
    private final TextExtractor unsupported = new NullTextExtractor();

    public TextExtractorFactory(List<TextExtractor> strategies) {
        strategies.forEach(strategy -> extractors.put(strategy.supportedType(), strategy));
    }

    /** Fallback to the Null Object for formats without a strategy (VIDEO, OTHER, ...). */
    public TextExtractor forType(Material.MaterialType type) {
        return extractors.getOrDefault(type, unsupported);
    }

    /** Types that currently have a text-extraction strategy. */
    public List<Material.MaterialType> supportedTypes() {
        return List.copyOf(extractors.keySet());
    }
}
