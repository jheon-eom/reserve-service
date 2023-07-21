package com.example.PointService.domain.point.service;

import com.example.PointService.domain.point.dto.SavePointRequestDto;
import com.example.PointService.domain.point.dto.UsePointRequestDto;
import com.example.PointService.domain.point.entity.Point;
import com.example.PointService.domain.point.entity.PointType;
import com.example.PointService.domain.point.repository.PointRepository;
import com.example.PointService.domain.point.ticket.TicketRepository;
import com.example.PointService.dummy.PointCreator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
@SpringBootTest
class PointServiceTest {
    @Autowired
    PointService pointService;
    @Autowired
    PointRepository pointRepository;
    @Autowired
    TicketRepository ticketRepository;

    @Test
    @DisplayName("적립금 합계 조회 테스트")
    void getPointTest() {
        // given
        savePointData();

        // when
        var points = pointService.getPoint(1L);

        // then
        assertTrue(points.amount().equals(15L));
    }

    @Test
    @DisplayName("적립금 적립 사용 복잡한 계산 조회 테스트")
    void getCalculatePointTest() {
        // given
        var savePoint1 = PointCreator.createPoint(1L, 1L, 10L, PointType.SAVE);
        var savePoint2 = PointCreator.createPoint(1L, 2L, 20L, PointType.SAVE);
        var savePoint3 = PointCreator.createPoint(1L, 2L, 30L, PointType.SAVE);
        pointRepository.save(savePoint1);
        pointRepository.save(savePoint2);
        pointRepository.save(savePoint3);

        // when
        pointService.usePoint(1L, new UsePointRequestDto(1L, 30L));
        var historyDtos = pointService.getPointHistory(1L,
                PageRequest.of(0, 10)).getContent();

        // then
        assertTrue(historyDtos.size() == 5); // 적립 포인트를 분할해서 사용함
    }

    @Test
    @DisplayName("적립금 적립 사용 내역 조회 테스트")
    void getPointHistoryTest() {
        // given
        for (int i = 0; i < 5; i++) {
            var savePoint = PointCreator.createPoint(1L, 1L, 10L, PointType.SAVE);
            pointRepository.save(savePoint);
        }

        // when
        var pointHistory = pointService.getPointHistory(1L, PageRequest.of(0, 10));

        // then
        assertTrue(pointHistory.getContent().size() == 5);
    }

    @Test
    @DisplayName("적립금 저장 테스트")
    void savePointTest() {
        // given
        savePointData();
        var memberId = 1L;
        var savePointRequestDto = new SavePointRequestDto(1L, 30L);

        // when
        var memberPointDto = pointService.savePoint(memberId, savePointRequestDto);

        // then
        assertTrue(memberPointDto.memberId().equals(1L));
        assertTrue(memberPointDto.amount().equals(45L));
    }

    @Test
    @DisplayName("최초 적립금 여러건 사용 테스트")
    void usePointManyTest() {
        // given
        var savePoint1 = PointCreator.createPoint(1L, 1L, 10L, PointType.SAVE);
        var savePoint2 = PointCreator.createPoint(1L, 2L, 20L, PointType.SAVE);
        var points = new ArrayList<Point>();
        points.add(savePoint1);
        points.add(savePoint2);
        pointRepository.saveAll(points);

        var usePointRequestDto = new UsePointRequestDto(2L, 15L);

        // when
        var memberPointDto = pointService.usePoint(1L, usePointRequestDto);

        // then
        assertTrue(memberPointDto.amount().equals(15L));
    }

    @Test
    @DisplayName("사용 금액이 한 건의 적립금 초과 시 다음 저장 포인트로 넘어가는지 확인 테스트")
    void usePointTest() {
        // given
        var savePoint1 = PointCreator.createPoint(1L, 1L, 10L, PointType.SAVE);
        var savePoint2 = PointCreator.createPoint(1L, 2L, 20L, PointType.SAVE);
        var points = new ArrayList<Point>();
        points.add(savePoint1);
        points.add(savePoint2);
        pointRepository.saveAll(points);

        var usePointRequestDto = new UsePointRequestDto(2L, 15L);

        // when
        var memberPointDto = pointService.usePoint(1L, usePointRequestDto);

        // then
        assertTrue(memberPointDto.amount().equals(15L));
    }

    @Test
    @DisplayName("포인트 사용 시 락 잠금 해제 테스트")
    void usePointLockTest() {
        // given
        var savePoint = PointCreator.createPoint(1L, 1L, 10L, PointType.SAVE);
        pointRepository.save(savePoint);

        // when
        pointService.usePoint(1L, new UsePointRequestDto(1L, 5L));

        // then
        Assertions.assertThrows(NoSuchElementException.class,
                () -> ticketRepository.findByMemberId(1L).get());
    }

    void savePointData() {
        var savePoint1 = PointCreator.createPoint(1L, 1L, 10L, PointType.SAVE);
        var savePoint2 = PointCreator.createPoint(1L, 2L, 20L, PointType.SAVE);
        var usePoint1 = PointCreator.createPoint(1L, 3L, 15L, PointType.USE);

        var points = new ArrayList<Point>();
        points.add(savePoint1);
        points.add(savePoint2);
        points.add(usePoint1);

        pointRepository.saveAll(points);
    }
}