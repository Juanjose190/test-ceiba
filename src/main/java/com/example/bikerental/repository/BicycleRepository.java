package com.example.bikerental.repository;

import com.example.bikerental.model.Bicycle;
import com.example.bikerental.model.BicycleStatus;
import com.example.bikerental.model.BicycleType;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class BicycleRepository {

    private final ConcurrentHashMap<String, Bicycle> bicycles = new ConcurrentHashMap<>();

    public Bicycle save(Bicycle bicycle) {
        bicycles.put(bicycle.getCode(), bicycle);
        return bicycle;
    }

    public Optional<Bicycle> findByCode(String code) {
        return Optional.ofNullable(bicycles.get(code));
    }

    public boolean existsByCode(String code) {
        return bicycles.containsKey(code);
    }

    public Collection<Bicycle> findAll() {
        return bicycles.values();
    }

    public Collection<Bicycle> findAvailable(BicycleType type) {
        return bicycles.values().stream()
                .filter(bicycle -> bicycle.getStatus() == BicycleStatus.DISPONIBLE)
                .filter(bicycle -> type == null || bicycle.getType() == type)
                .toList();
    }
}
