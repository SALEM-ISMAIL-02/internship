package com.example.elearning.extract;

import com.example.elearning.entity.Material;

import java.nio.file.Path;

/**
 * NULL OBJECT: stands in for formats without an extraction strategy (VIDEO, OTHER),
 * so callers never have to null-check the strategy lookup.
 * LSP note: it answers "" and reports OTHER; the factory uses it as the
 * default fallback for any unmapped type, which is documented in
 * {@link TextExtractorFactory#forType}.
 */
public class NullTextExtractor implements TextExtractor {

    @Override
    public String extract(Path path) {
        return "";
    }

    @Override
    public Material.MaterialType supportedType() {
        return Material.MaterialType.OTHER;
    }
}
