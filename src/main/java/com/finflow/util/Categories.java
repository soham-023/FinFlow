package com.finflow.util;

import java.util.Locale;

/**
 * Single place for the rule that turns user-supplied category names into their stored form.
 * <p>
 * Categories are compared by exact string in queries, so every write and every lookup must
 * normalize the same way. {@link Locale#ROOT} is used so the result does not depend on the
 * server's default locale (e.g. the Turkish dotless-i problem with {@code "fit".toUpperCase()}).
 */
public final class Categories {

    private Categories() {
    }

    /**
     * Trim surrounding whitespace and upper-case the category, e.g. {@code " food "} becomes {@code "FOOD"}.
     *
     * @return the normalized category, or {@code null} if {@code category} is {@code null}
     */
    public static String normalize(String category) {
        return category == null ? null : category.trim().toUpperCase(Locale.ROOT);
    }
}
