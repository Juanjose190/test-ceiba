package com.example.bikerental.dto;

import java.time.LocalDateTime;

public record FinishRentalRequest(
        LocalDateTime endTime
) {
}
