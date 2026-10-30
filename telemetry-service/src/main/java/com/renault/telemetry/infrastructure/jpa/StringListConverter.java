package com.renault.telemetry.infrastructure.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;

@Converter
public class StringListConverter implements AttributeConverter<List<String>, String> {
    @Override public String convertToDatabaseColumn(List<String> list) {
        return list == null ? "" : String.join(",", list);
    }
    @Override public List<String> convertToEntityAttribute(String db) {
        return db == null || db.isBlank() ? List.of() : Arrays.asList(db.split(","));
    }
}