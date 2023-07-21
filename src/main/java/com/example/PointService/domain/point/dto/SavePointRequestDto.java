package com.example.PointService.domain.point.dto;

import java.util.Objects;

public record SavePointRequestDto(Long trxId, Long amount) {

    public SavePointRequestDto {
        Objects.requireNonNull(trxId);
        Objects.requireNonNull(amount);
    }
}
