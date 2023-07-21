package com.example.PointService.domain.point.service;

import com.example.PointService.domain.point.dto.*;
import com.example.PointService.domain.point.entity.Point;
import com.example.PointService.domain.point.entity.PointType;
import com.example.PointService.domain.point.repository.PointRepository;
import com.example.PointService.domain.point.ticket.Ticket;
import com.example.PointService.domain.point.ticket.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PointService {
    private final PointRepository pointRepository;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public PointDto getPoint(Long memberId) {
        checkMemberId(memberId);

        var points = pointRepository.findAllByMemberId(memberId);
        var totalPoint = calculateTotalPoint(points);

        return new PointDto(memberId, totalPoint);
    }

    @Transactional(readOnly = true)
    public Page<PointHistoryDto> getPointHistory(Long memberId, PageRequest pageRequest) {
        checkMemberId(memberId);

        var pointHistoryPages = pointRepository.findAllPageByMemberId(memberId, pageRequest);

        return pointHistoryPages.map(history -> new PointHistoryDto(memberId, history.getAmount(),
                history.getType(), history.getCreatedAt()));
    }

    @Transactional
    public PointDto savePoint(Long memberId, SavePointRequestDto savePointRequestDto) {
        checkMemberId(memberId);
        Assert.isTrue(!savePointRequestDto.trxId().equals(0L), "해당하는 거래가 없습니다.");
        Assert.isTrue(!savePointRequestDto.amount().equals(0L), "적립할 포인트가 없습니다.");

        // 적립
        var savePoint = Point.createSavePoint(memberId, savePointRequestDto);
        pointRepository.save(savePoint);

        // 적립금 합계 조회
        var points = pointRepository.findAllByMemberId(memberId);
        var totalPoint = calculateTotalPoint(points);

        return new PointDto(memberId, totalPoint);
    }

    @Transactional
    public PointDto usePoint(Long memberId, UsePointRequestDto usePointRequestDto) {
        checkMemberId(memberId);
        Assert.isTrue(!usePointRequestDto.trxId().equals(0L), "해당하는 거래가 없습니다.");
        Assert.isTrue(!usePointRequestDto.amount().equals(0L), "사용 입력 포인트가 0원입니다.");

        // DB LOCK 확인 후 잠금
        ticketRepository.findByMemberId(memberId).ifPresentOrElse(ticket -> {
            throw new IllegalArgumentException("잠시 후에 다시 시도해주세요.");
        }, () -> ticketRepository.save(new Ticket(memberId)));

        var points = pointRepository.findAllByMemberId(memberId);
        Assert.isTrue(!points.isEmpty(), "사용 가능한 포인트가 없습니다.");

        var savePoints = getSavePoints(points);
        Assert.isTrue(!savePoints.isEmpty(), "사용 가능한 포인트가 없습니다.");
        var usedPoints = getUsedPoints(points);
        var cancelPoints = getCancelPoints(points);

        var validTotalPoint = calculateTotalPoint(points);
        Assert.isTrue(validTotalPoint >= usePointRequestDto.amount(), "적립금이 부족합니다.");

        // 포인트 사용
        var toUsePoints = calculateUsePoint(memberId, usePointRequestDto, savePoints, usedPoints, cancelPoints);
        pointRepository.saveAll(toUsePoints);

        toUsePoints.forEach(points::add);
        var totalPoint = calculateTotalPoint(points);

        // DB LOCK 해제
        ticketRepository.deleteByMemberId(memberId);

        return new PointDto(memberId, totalPoint);
    }

    @Transactional
    public PointDto cancelPoint(Long memberId, CancelPointRequestDto cancelPointRequestDto) {
        checkMemberId(memberId);
        Assert.isTrue(!cancelPointRequestDto.trxId().equals(0L), "해당하는 거래가 없습니다.");

        // DB LOCK 확인 후 잠금
        ticketRepository.findByMemberId(memberId).ifPresentOrElse(ticket -> {
            throw new IllegalArgumentException("잠시 후에 다시 시도해주세요.");
        }, () -> ticketRepository.save(new Ticket(memberId)));

        // 해당 거래 아이디로 사용된 포인트 조회
        var usedPoints = pointRepository.findAllByTrxIdAndType(cancelPointRequestDto.trxId(), PointType.USE);
        var sumUsedPointAmount = 0L;
        var cancelPoints = new ArrayList<Point>();
        for (Point usePoint : usedPoints) {
            sumUsedPointAmount += usePoint.getAmount();
            cancelPoints.add(Point.createCancelPoint(memberId, cancelPointRequestDto.trxId(), usePoint));
        }
        Assert.isTrue(cancelPointRequestDto.amount().equals(sumUsedPointAmount), "포인트 취소 금액이 맞지 않습니다.");
        pointRepository.saveAll(cancelPoints);

        var points = pointRepository.findAllByMemberId(memberId);
        var totalPoint = calculateTotalPoint(points);

        // DB LOCK 해제
        ticketRepository.deleteByMemberId(memberId);

        return new PointDto(memberId, totalPoint);
    }

    private List<Point> calculateUsePoint(Long memberId, UsePointRequestDto usePointRequestDto,
                           List<Point> savePoints, List<Point> usedPoints, List<Point> cancelPoints) {
        var amount = usePointRequestDto.amount();
        var toUsePoints = new ArrayList<Point>();

        // 환불 포인트가 있을 경우 해당 사용 포인트 제거
        if (!cancelPoints.isEmpty()) {
            usedPoints.removeIf(usedPoint -> cancelPoints.stream().anyMatch(cancelPoint ->
                    cancelPoint.getCancelPointId().equals(usedPoint.getId())));
        }

        // 최초 적립금 사용 연산
        if (usedPoints.isEmpty()) {
            for (Point savePoint : savePoints) {
                if (amount.equals(0L)) {
                    break;
                }
                if (savePoint.getAmount() >= amount) {
                    var toUsePoint = Point.createUsePoint(memberId, savePoint.getId(),
                            usePointRequestDto.trxId(), amount, savePoint.getExpiredPeriod());
                    toUsePoints.add(toUsePoint);
                    amount = 0L;
                } else if (savePoint.getAmount() < amount) {
                    var toUsePoint = Point.createUsePoint(memberId, savePoint.getId(),
                            usePointRequestDto.trxId(), savePoint.getAmount(), savePoint.getExpiredPeriod());
                    toUsePoints.add(toUsePoint);
                    amount -= savePoint.getAmount();
                }
            }
        // 사용 내역이 존재, 마지막으로 사용된 savePoint 잔액 연산
        } else {
            var lastUsedPoint = usedPoints.get(0);
            var minusPoint = 0L;
            var index = 0;
            for (Point savePoint : savePoints) {
                if (lastUsedPoint.getUsedPointId().equals(savePoint.getId())) {
                    var usedIdOfLastUsedPoint = savePoint.getId();
                    for (Point usedPoint : usedPoints) {
                        if (usedIdOfLastUsedPoint.equals(usedPoint.getUsedPointId())) {
                            minusPoint += usedPoint.getAmount();
                        }
                    }
                    break;
                }
                index++;
            }
            var resetAmount = savePoints.get(index).getAmount();
            savePoints.get(index).minus(minusPoint);
            for (Point savePoint : savePoints) {
                if (savePoint.getId() < lastUsedPoint.getUsedPointId() || savePoint.getAmount().equals(0L)) {
                    continue;
                }
                if (amount.equals(0L)) {
                    break;
                }
                if (savePoint.getAmount() >= amount) {
                    var toUsePoint = Point.createUsePoint(memberId, savePoint.getId(),
                            usePointRequestDto.trxId(), amount, savePoint.getExpiredPeriod());
                    toUsePoints.add(toUsePoint);
                    amount = 0L;
                } else if (savePoint.getAmount() < amount) {
                    var toUsePoint = Point.createUsePoint(memberId, savePoint.getId(),
                            usePointRequestDto.trxId(), savePoint.getAmount(), savePoint.getExpiredPeriod());
                    toUsePoints.add(toUsePoint);
                    amount -= savePoint.getAmount();
                }
            }
            savePoints.get(index).reset(resetAmount);
        }

        return toUsePoints;
    }

    private List<Point> getSavePoints(List<Point> points) {
        return points.stream()
                .filter(p -> p.getType().equals(PointType.SAVE))
                .filter(p -> p.getExpiredPeriod().isAfter(LocalDate.now()))
                .sorted(Comparator.comparing(Point::getExpiredPeriod).reversed())
                .collect(Collectors.toList());
    }

    private List<Point> getUsedPoints(List<Point> points) {
        return points.stream()
                .filter(p -> p.getType().equals(PointType.USE))
                .filter(p -> p.getExpiredPeriod().isAfter(LocalDate.now()))
                .sorted(Comparator.comparing(Point::getUsedPointId).reversed())
                .collect(Collectors.toList());
    }

    private List<Point> getCancelPoints(List<Point> point) {
        return point.stream()
                .filter(p -> p.getType().equals(PointType.CANCEL))
                .filter(p -> p.getExpiredPeriod().isAfter(LocalDate.now()))
                .sorted(Comparator.comparing(Point::getCancelPointId).reversed())
                .collect(Collectors.toList());
    }

    private Long calculateTotalPoint(List<Point> points) {
        var totalPoint = 0L;

        for (Point p : points) {
            if (p.getType().equals(PointType.SAVE) && p.getExpiredPeriod().isAfter(LocalDate.now())) {
                totalPoint += p.getAmount();
            } else if (p.getType().equals(PointType.USE)) {
                totalPoint -= p.getAmount();
            } else if (p.getType().equals(PointType.CANCEL)) {
                totalPoint += p.getAmount();
            }
        }

        return totalPoint;
    }

    private void checkMemberId(Long memberId) {
        if (memberId.equals(0L) || memberId == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
    }
}
