package com.example.PointService.domain.point.dto;

import com.example.PointService.domain.point.entity.PointType;

import java.time.LocalDateTime;

public record PointHistoryDto(
        Long memberId,
        Long amount,
        PointType type,
        LocalDateTime createdAt
) {

}
