package com.example.electrical_preorder_system_backend.util;

import java.text.Normalizer;
import java.util.Locale;

public class SlugUtil {
    public static String generateSlug(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("[^\\p{ASCII}]", "");
        slug = slug.toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return slug;
    }
}
