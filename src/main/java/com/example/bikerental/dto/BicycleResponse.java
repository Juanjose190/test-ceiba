package com.example.bikerental.dto;

import com.example.bikerental.model.Bicycle;
import com.example.bikerental.model.BicycleStatus;
import com.example.bikerental.model.BicycleType;

public record BicycleResponse(
        String code,
        BicycleType type,
        BicycleStatus status
) {
    public static BicycleResponse from(Bicycle bicycle) {
        return new BicycleResponse(bicycle.getCode(), bicycle.getType(), bicycle.getStatus());
    }
}
