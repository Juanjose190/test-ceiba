package com.example.bikerental.service;

import com.example.bikerental.exception.BusinessException;
import com.example.bikerental.model.Bicycle;
import com.example.bikerental.model.BicycleStatus;
import com.example.bikerental.model.Rental;
import com.example.bikerental.repository.BicycleRepository;
import com.example.bikerental.repository.RentalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RentalService {

    private final BicycleRepository bicycleRepository;
    private final RentalRepository rentalRepository;
    private final RentalCostCalculator rentalCostCalculator;
    private final Clock clock;

    public RentalService(BicycleRepository bicycleRepository, RentalRepository rentalRepository,
                         RentalCostCalculator rentalCostCalculator, Clock clock) {
        this.bicycleRepository = bicycleRepository;
        this.rentalRepository = rentalRepository;
        this.rentalCostCalculator = rentalCostCalculator;
        this.clock = clock;
    }

    @Transactional
    public Rental startRental(String bicycleCode, String customerName, LocalDateTime startTime, int estimatedDurationHours) {
        Bicycle bicycle = bicycleRepository.findByCodeForUpdate(bicycleCode.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "No existe una bicicleta con código " + bicycleCode));

        if (bicycle.getStatus() != BicycleStatus.DISPONIBLE) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "La bicicleta " + bicycle.getCode() + " no está disponible. Estado actual: " + bicycle.getStatus());
        }

        LocalDateTime effectiveStartTime = startTime != null ? startTime : LocalDateTime.now(clock);
        Rental rental = new Rental(UUID.randomUUID(), bicycle.getCode(), bicycle.getType(),
                customerName.trim(), effectiveStartTime, estimatedDurationHours);

        bicycle.setStatus(BicycleStatus.ALQUILADA);
        bicycleRepository.save(bicycle);
        return rentalRepository.save(rental);
    }

    @Transactional
    public Rental finishRental(UUID rentalId, LocalDateTime endTime) {
        Rental rental = rentalRepository.findByIdForUpdate(rentalId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "No existe un alquiler con id " + rentalId));

        if (rental.isFinished()) {
            throw new BusinessException(HttpStatus.CONFLICT, "El alquiler " + rentalId + " ya fue finalizado");
        }

        LocalDateTime effectiveEndTime = endTime != null ? endTime : LocalDateTime.now(clock);
        if (!effectiveEndTime.isAfter(rental.getStartTime())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La hora de devolución debe ser posterior a la hora de inicio");
        }

        Duration actualDuration = Duration.between(rental.getStartTime(), effectiveEndTime);
        RentalCost rentalCost = rentalCostCalculator.calculate(rental.getBicycleType(), actualDuration, rental.getEstimatedDurationHours());
        rental.finish(effectiveEndTime, actualDuration, rentalCost.total(), rentalCost.fined());

        Bicycle bicycle = bicycleRepository.findByCodeForUpdate(rental.getBicycleCode())
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se encontró la bicicleta asociada al alquiler " + rentalId));
        bicycle.setStatus(BicycleStatus.DISPONIBLE);
        bicycleRepository.save(bicycle);

        return rentalRepository.save(rental);
    }

    @Transactional(readOnly = true)
    public List<Rental> findHistoryByBicycleCode(String bicycleCode) {
        String normalizedCode = bicycleCode.trim().toUpperCase();
        if (!bicycleRepository.existsById(normalizedCode)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "No existe una bicicleta con código " + bicycleCode);
        }
        return rentalRepository.findByBicycleCodeOrderByStartTimeAsc(normalizedCode).stream().toList();
    }
}
