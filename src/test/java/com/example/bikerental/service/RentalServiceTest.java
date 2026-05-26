package com.example.bikerental.service;

import com.example.bikerental.exception.BusinessException;
import com.example.bikerental.model.Bicycle;
import com.example.bikerental.model.BicycleStatus;
import com.example.bikerental.model.BicycleType;
import com.example.bikerental.model.Rental;
import com.example.bikerental.repository.BicycleRepository;
import com.example.bikerental.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RentalServiceTest {

    private BicycleRepository bicycleRepository;
    private RentalService rentalService;

    @BeforeEach
    void setUp() {
        bicycleRepository = new BicycleRepository();
        RentalRepository rentalRepository = new RentalRepository();
        Clock fixedClock = Clock.fixed(Instant.parse("2026-04-28T10:00:00Z"), ZoneId.of("UTC"));
        rentalService = new RentalService(bicycleRepository, rentalRepository, new RentalCostCalculator(), fixedClock);
    }

    @Test
    void startsRentalOnlyWhenBicycleIsAvailable() {
        bicycleRepository.save(new Bicycle("BIC-004", BicycleType.MONTANA, BicycleStatus.EN_MANTENIMIENTO));

        assertThatThrownBy(() -> rentalService.startRental("BIC-004", "Ana", LocalDateTime.now(), 2))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("no está disponible")
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void changesBicycleStatusWhenStartingAndFinishingRental() {
        bicycleRepository.save(new Bicycle("BIC-001", BicycleType.URBANA, BicycleStatus.DISPONIBLE));
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 28, 8, 0);

        Rental rental = rentalService.startRental("BIC-001", "Carlos", startTime, 2);

        assertThat(bicycleRepository.findByCode("BIC-001")).get()
                .extracting(Bicycle::getStatus)
                .isEqualTo(BicycleStatus.ALQUILADA);

        rentalService.finishRental(rental.getId(), startTime.plusMinutes(130));

        assertThat(bicycleRepository.findByCode("BIC-001")).get()
                .extracting(Bicycle::getStatus)
                .isEqualTo(BicycleStatus.DISPONIBLE);
    }

    @Test
    void rejectsFinishingRentalTwice() {
        bicycleRepository.save(new Bicycle("BIC-002", BicycleType.MONTANA, BicycleStatus.DISPONIBLE));
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 28, 9, 0);
        Rental rental = rentalService.startRental("BIC-002", "Luisa", startTime, 1);
        rentalService.finishRental(rental.getId(), startTime.plusHours(1));

        assertThatThrownBy(() -> rentalService.finishRental(rental.getId(), startTime.plusHours(2)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya fue finalizado")
                .extracting("status")
                .isEqualTo(HttpStatus.CONFLICT);
    }
}
