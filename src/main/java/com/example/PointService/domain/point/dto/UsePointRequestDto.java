package com.example.PointService.domain.point.dto;

import java.util.Objects;

public record UsePointRequestDto(Long trxId, Long amount) {

    public UsePointRequestDto {
        Objects.requireNonNull(trxId);
        Objects.requireNonNull(amount);
    }
}
