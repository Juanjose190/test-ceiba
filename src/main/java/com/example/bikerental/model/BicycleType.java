package com.example.bikerental.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;

public enum BicycleType {
    URBANA("URBANA", new BigDecimal("3500")),
    MONTANA("MONTAÑA", new BigDecimal("5000")),
    ELECTRICA("ELÉCTRICA", new BigDecimal("7500"));

    private final String label;
    private final BigDecimal hourlyRate;

    BicycleType(String label, BigDecimal hourlyRate) {
        this.label = label;
        this.hourlyRate = hourlyRate;
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    @JsonCreator
    public static BicycleType fromJson(String value) {
        String normalizedValue = normalize(value);
        return Arrays.stream(values())
                .filter(type -> normalize(type.name()).equals(normalizedValue) || normalize(type.label).equals(normalizedValue))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de bicicleta inválido: " + value));
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase();
    }
}
