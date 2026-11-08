package com.finflow.entity.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class YearMonthConverterTest {

    private final YearMonthConverter converter = new YearMonthConverter();

    @Test
    @DisplayName("Should convert YearMonth to ISO string for the database")
    void shouldConvertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(YearMonth.of(2026, 10))).isEqualTo("2026-10");
        assertThat(converter.convertToDatabaseColumn(YearMonth.of(2026, 1))).isEqualTo("2026-01");
    }

    @Test
    @DisplayName("Should parse database string back into YearMonth")
    void shouldConvertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute("2026-10")).isEqualTo(YearMonth.of(2026, 10));
    }

    @Test
    @DisplayName("Should pass null through in both directions")
    void shouldHandleNulls() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    @DisplayName("Round trip should preserve the value")
    void shouldRoundTrip() {
        YearMonth original = YearMonth.of(2025, 12);
        assertThat(converter.convertToEntityAttribute(converter.convertToDatabaseColumn(original)))
                .isEqualTo(original);
    }
}
