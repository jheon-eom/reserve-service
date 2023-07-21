package com.example.PointService.domain.point.entity;

import com.example.PointService.domain.point.dto.SavePointRequestDto;
import com.example.PointService.util.DateUtil;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long memberId;

    @Column
    private Long trxId;

    @Column
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column
    private PointType type;

    @Column
    private Long usedPointId;

    @Column
    private Long cancelPointId;

    @Column
    private LocalDate expiredPeriod;

    @Column
    private LocalDateTime createdAt;

    @Builder
    private Point(Long id, Long memberId, Long trxId, Long amount, PointType type,
                 Long usedPointId, Long cancelPointId, LocalDate expiredPeriod, LocalDateTime createdAt) {
        this.id = id;
        this.memberId = memberId;
        this.trxId = trxId;
        this.amount = amount;
        this.type = type;
        this.usedPointId = usedPointId;
        this.cancelPointId = cancelPointId;
        this.expiredPeriod = expiredPeriod;
        this.createdAt = createdAt;
    }

    public static Point createSavePoint(Long memberId, SavePointRequestDto savePointRequestDto) {
        return Point.builder()
                .memberId(memberId)
                .trxId(savePointRequestDto.trxId())
                .amount(savePointRequestDto.amount())
                .type(PointType.SAVE)
                .usedPointId(null)
                .cancelPointId(null)
                .expiredPeriod(DateUtil.createExpireDate())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Point createUsePoint(Long memberId, Long pointId, Long trxId, Long amount,
                                       LocalDate expiredPeriod) {
        return Point.builder()
                .memberId(memberId)
                .trxId(trxId)
                .amount(amount)
                .type(PointType.USE)
                .usedPointId(pointId)
                .cancelPointId(null)
                .expiredPeriod(expiredPeriod)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Point createCancelPoint(Long memberId, Long trxId, Point usePoint) {
        return Point.builder()
                .memberId(memberId)
                .trxId(trxId)
                .amount(usePoint.getAmount())
                .type(PointType.CANCEL)
                .usedPointId(null)
                .cancelPointId(usePoint.getId())
                .expiredPeriod(usePoint.getExpiredPeriod())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void minus(Long minusPoint) {
        this.amount -= minusPoint;
    }

    public void reset(Long resetAmount) {
        this.amount = resetAmount;
    }
}
