package com.example.PointService.domain.point.dto;

import java.util.Objects;

public record CancelPointRequestDto(Long trxId, Long amount) {

    public CancelPointRequestDto {
        Objects.requireNonNull(trxId);
        Objects.requireNonNull(amount);
    }
}
