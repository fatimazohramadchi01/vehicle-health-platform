package com.renault.telemetry.infrastructure.jpa;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class DoubleListConverter implements AttributeConverter<List<Double>, String> {
    @Override public String convertToDatabaseColumn(List<Double> list) {
        return list == null ? "" : list.stream().map(String::valueOf).collect(Collectors.joining(","));
    }
    @Override public List<Double> convertToEntityAttribute(String db) {
        return db == null || db.isBlank() ? List.of()
                : Arrays.stream(db.split(",")).map(Double::valueOf).toList();
    }
}