package com.example.bikerental.service;

import com.example.bikerental.exception.BusinessException;
import com.example.bikerental.model.Bicycle;
import com.example.bikerental.model.BicycleStatus;
import com.example.bikerental.model.BicycleType;
import com.example.bikerental.repository.BicycleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BicycleService {

    private final BicycleRepository bicycleRepository;

    public BicycleService(BicycleRepository bicycleRepository) {
        this.bicycleRepository = bicycleRepository;
    }

    public Bicycle register(String code, BicycleType type, BicycleStatus status) {
        String normalizedCode = code.trim().toUpperCase();
        if (bicycleRepository.existsByCode(normalizedCode)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya existe una bicicleta con código " + normalizedCode);
        }
        return bicycleRepository.save(new Bicycle(normalizedCode, type, status));
    }

    public List<Bicycle> findAvailable(BicycleType type) {
        return bicycleRepository.findAvailable(type).stream()
                .sorted((left, right) -> left.getCode().compareTo(right.getCode()))
                .toList();
    }

    public Bicycle findByCodeOrThrow(String code) {
        return bicycleRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "No existe una bicicleta con código " + code));
    }
}
