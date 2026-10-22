package com.finflow.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.YearMonth;

/**
 * JPA converter for YearMonth ↔ String (e.g. "2026-10").
 * Applied automatically to all YearMonth fields.
 */
@Converter(autoApply = true)
public class YearMonthConverter implements AttributeConverter<YearMonth, String> {

    @Override
    public String convertToDatabaseColumn(YearMonth yearMonth) {
        return yearMonth != null ? yearMonth.toString() : null;
    }

    @Override
    public YearMonth convertToEntityAttribute(String dbValue) {
        return dbValue != null ? YearMonth.parse(dbValue) : null;
    }
}
