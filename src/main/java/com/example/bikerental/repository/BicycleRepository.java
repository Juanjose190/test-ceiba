package com.example.bikerental.repository;

import com.example.bikerental.model.Bicycle;
import com.example.bikerental.model.BicycleStatus;
import com.example.bikerental.model.BicycleType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BicycleRepository extends JpaRepository<Bicycle, String> {

    List<Bicycle> findByStatus(BicycleStatus status);

    List<Bicycle> findByStatusAndType(BicycleStatus status, BicycleType type);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bicycle b where b.code = :code")
    Optional<Bicycle> findByCodeForUpdate(@Param("code") String code);
}
