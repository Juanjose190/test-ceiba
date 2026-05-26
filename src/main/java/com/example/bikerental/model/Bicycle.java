package com.example.bikerental.model;

import java.util.Objects;

public class Bicycle {

    private final String code;
    private final BicycleType type;
    private BicycleStatus status;

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
