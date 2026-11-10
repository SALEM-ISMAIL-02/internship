package com.example.elearning.service;

import com.example.elearning.entity.Material;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * OCP: extension -&gt; MaterialType registry.
 * New formats are added by registering entries, not by editing if-chains.
 * SRP: ONLY answers "which type is this filename?".
 */
@Component
public class FileTypeResolver {

    private final Map<String, Material.MaterialType> extensionToType = new LinkedHashMap<>();

    public FileTypeResolver() {
        register("pdf", Material.MaterialType.PDF);
        register("doc", Material.MaterialType.DOC);
        register("docx", Material.MaterialType.DOC);
        register("ppt", Material.MaterialType.PPT);
        register("pptx", Material.MaterialType.PPT);
        register("mp4", Material.MaterialType.VIDEO);
        register("avi", Material.MaterialType.VIDEO);
        register("mov", Material.MaterialType.VIDEO);
    }

    /** Extension point: custom formats without touching callers. */
    public void register(String extension, Material.MaterialType type) {
        extensionToType.put(extension.toLowerCase(Locale.ROOT), type);
    }

    public Material.MaterialType resolve(String filename) {
        String n = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        int dot = n.lastIndexOf('.');
        String ext = dot >= 0 ? n.substring(dot + 1) : "";
        return extensionToType.getOrDefault(ext, Material.MaterialType.OTHER);
    }
}
