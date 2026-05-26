package com.example.bikerental.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "bicycles")
public class Bicycle {

    @Id
    @Column(nullable = false, updatable = false, length = 30)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BicycleType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BicycleStatus status;

    protected Bicycle() {
    }

    public Bicycle(String code, BicycleType type, BicycleStatus status) {
        this.code = Objects.requireNonNull(code);
        this.type = Objects.requireNonNull(type);
        this.status = Objects.requireNonNull(status);
    }

    public String getCode() {
        return code;
    }

    public BicycleType getType() {
        return type;
    }

    public BicycleStatus getStatus() {
        return status;
    }

    public void setStatus(BicycleStatus status) {
        this.status = Objects.requireNonNull(status);
    }
}
