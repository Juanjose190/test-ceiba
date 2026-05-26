package com.example.bikerental.repository;

import com.example.bikerental.model.Rental;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class RentalRepository {

    private final ConcurrentHashMap<UUID, Rental> rentals = new ConcurrentHashMap<>();

    public Rental save(Rental rental) {
        rentals.put(rental.getId(), rental);
        return rental;
    }

    public Optional<Rental> findById(UUID id) {
        return Optional.ofNullable(rentals.get(id));
    }

    public Collection<Rental> findByBicycleCode(String bicycleCode) {
        return rentals.values().stream()
                .filter(rental -> rental.getBicycleCode().equals(bicycleCode))
                .sorted((left, right) -> left.getStartTime().compareTo(right.getStartTime()))
                .toList();
    }
}
