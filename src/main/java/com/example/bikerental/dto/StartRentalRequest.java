package com.example.bikerental.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record StartRentalRequest(
        @NotBlank String bicycleCode,
        @NotBlank String customerName,
        LocalDateTime startTime,
        @NotNull @Min(1) Integer estimatedDurationHours
) {
}
