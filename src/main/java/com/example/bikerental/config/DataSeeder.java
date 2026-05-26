package com.example.bikerental.config;

import com.example.bikerental.model.BicycleStatus;
import com.example.bikerental.model.BicycleType;
import com.example.bikerental.service.BicycleService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final BicycleService bicycleService;

    public DataSeeder(BicycleService bicycleService) {
        this.bicycleService = bicycleService;
    }

    @Override
    public void run(String... args) {
        registerIfMissing("BIC-001", BicycleType.URBANA, BicycleStatus.DISPONIBLE);
        registerIfMissing("BIC-002", BicycleType.MONTANA, BicycleStatus.DISPONIBLE);
        registerIfMissing("BIC-003", BicycleType.ELECTRICA, BicycleStatus.DISPONIBLE);
        registerIfMissing("BIC-004", BicycleType.MONTANA, BicycleStatus.EN_MANTENIMIENTO);
        registerIfMissing("BIC-005", BicycleType.URBANA, BicycleStatus.DISPONIBLE);
    }

    private void registerIfMissing(String code, BicycleType type, BicycleStatus status) {
        if (!bicycleService.existsByCode(code)) {
            bicycleService.register(code, type, status);
        }
    }
}
