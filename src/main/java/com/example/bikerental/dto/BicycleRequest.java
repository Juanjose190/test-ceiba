package com.example.bikerental.dto;

import com.example.bikerental.model.BicycleStatus;
import com.example.bikerental.model.BicycleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BicycleRequest(
        @NotBlank String code,
        @NotNull BicycleType type,
        @NotNull BicycleStatus status
) {
}
