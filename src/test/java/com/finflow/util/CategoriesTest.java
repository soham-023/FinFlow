package com.finflow.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class CategoriesTest {

    @Test
    @DisplayName("Should trim and upper-case categories")
    void shouldTrimAndUpperCase() {
        assertThat(Categories.normalize("  food ")).isEqualTo("FOOD");
        assertThat(Categories.normalize("Rent")).isEqualTo("RENT");
    }

    @Test
    @DisplayName("Should pass null through")
    void shouldHandleNull() {
        assertThat(Categories.normalize(null)).isNull();
    }

    @Test
    @DisplayName("Should not depend on the default locale")
    void shouldBeLocaleIndependent() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertThat(Categories.normalize("fitness")).isEqualTo("FITNESS");
        } finally {
            Locale.setDefault(original);
        }
    }
}
