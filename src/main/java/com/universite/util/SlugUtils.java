package com.universite.util;

import java.text.Normalizer;
import java.util.function.Predicate;

public final class SlugUtils {

    private SlugUtils() {
    }

    public static String toSlug(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "item" : normalized;
    }

    public static String resolveSlug(String requestedSlug, String titre, Predicate<String> slugTaken) {
        String base = requestedSlug != null && !requestedSlug.isBlank()
                ? toSlug(requestedSlug)
                : toSlug(titre);
        if (base.isBlank()) {
            throw new RuntimeException("Impossible de générer un identifiant (slug)");
        }
        if (!slugTaken.test(base)) {
            return base;
        }
        int suffix = 2;
        while (slugTaken.test(base + "-" + suffix)) {
            suffix++;
        }
        return base + "-" + suffix;
    }
}
