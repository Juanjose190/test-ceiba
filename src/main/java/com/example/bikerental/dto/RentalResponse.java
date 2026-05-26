package com.example.bikerental.dto;

import com.example.bikerental.model.Rental;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RentalResponse(
        UUID id,
        String bicycleCode,
        String customerName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer estimatedDurationHours,
        Long actualDurationMinutes,
        BigDecimal totalCost,
        boolean fined
) {
    public static RentalResponse from(Rental rental) {
        Long actualDurationMinutes = rental.getActualDuration() == null ? null : rental.getActualDuration().toMinutes();
        return new RentalResponse(
                rental.getId(),
                rental.getBicycleCode(),
                rental.getCustomerName(),
                rental.getStartTime(),
                rental.getEndTime(),
                rental.getEstimatedDurationHours(),
                actualDurationMinutes,
                rental.getTotalCost(),
                rental.isFined()
        );
    }
}
