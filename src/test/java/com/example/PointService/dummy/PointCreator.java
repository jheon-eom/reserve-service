package com.example.PointService.dummy;

import com.example.PointService.domain.point.entity.Point;
import com.example.PointService.domain.point.entity.PointType;
import com.example.PointService.util.DateUtil;

import java.time.LocalDateTime;

public class PointCreator {

    public static Point createPoint(Long memberId, Long trxId, Long amount, PointType type) {
        return Point.builder()
                .memberId(memberId)
                .trxId(trxId)
                .amount(amount)
                .type(type)
                .expiredPeriod(DateUtil.createExpireDate())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
