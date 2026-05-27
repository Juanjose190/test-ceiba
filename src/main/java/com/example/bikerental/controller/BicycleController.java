package com.example.bikerental.controller;

import com.example.bikerental.dto.BicycleRequest;
import com.example.bikerental.dto.BicycleResponse;
import com.example.bikerental.dto.RentalResponse;
import com.example.bikerental.model.BicycleType;
import com.example.bikerental.service.BicycleService;
import com.example.bikerental.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/bicicletas", produces = MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8")
public class BicycleController {

    private final BicycleService bicycleService;
    private final RentalService rentalService;

    public BicycleController(BicycleService bicycleService, RentalService rentalService) {
        this.bicycleService = bicycleService;
        this.rentalService = rentalService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BicycleResponse register(@Valid @RequestBody BicycleRequest request) {
        return BicycleResponse.from(bicycleService.register(request.code(), request.type(), request.status()));
    }

    @GetMapping("/disponibles")
    public List<BicycleResponse> findAvailable(@RequestParam(required = false) BicycleType type) {
        return bicycleService.findAvailable(type).stream()
                .map(BicycleResponse::from)
                .toList();
    }

    @GetMapping("/{code}/alquileres")
    public List<RentalResponse> findRentalHistory(@PathVariable String code) {
        return rentalService.findHistoryByBicycleCode(code).stream()
                .map(RentalResponse::from)
                .toList();
    }
}
