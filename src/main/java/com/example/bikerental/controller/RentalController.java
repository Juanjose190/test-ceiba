package com.example.bikerental.controller;

import com.example.bikerental.dto.FinishRentalRequest;
import com.example.bikerental.dto.RentalResponse;
import com.example.bikerental.dto.StartRentalRequest;
import com.example.bikerental.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/alquileres", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RentalResponse startRental(@Valid @RequestBody StartRentalRequest request) {
        return RentalResponse.from(rentalService.startRental(
                request.bicycleCode(),
                request.customerName(),
                request.startTime(),
                request.estimatedDurationHours()
        ));
    }

    @PutMapping("/{id}/finalizar")
    public RentalResponse finishRental(@PathVariable UUID id, @RequestBody(required = false) FinishRentalRequest request) {
        return RentalResponse.from(rentalService.finishRental(id, request == null ? null : request.endTime()));
    }
}
