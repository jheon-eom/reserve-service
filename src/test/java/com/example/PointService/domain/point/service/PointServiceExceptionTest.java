package com.example.PointService.domain.point.service;

import com.example.PointService.domain.point.dto.CancelPointRequestDto;
import com.example.PointService.domain.point.dto.SavePointRequestDto;
import com.example.PointService.domain.point.dto.UsePointRequestDto;
import com.example.PointService.domain.point.entity.PointType;
import com.example.PointService.domain.point.repository.PointRepository;
import com.example.PointService.dummy.PointCreator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest
public class PointServiceExceptionTest {
    @Autowired
    PointService pointService;
    @Autowired
    PointRepository pointRepository;

    @Test
    @DisplayName("포인트 적립 시 0원이면 예외")
    void savePointZeroExceptionTest() {
        // given
        var memberId = 1L;
        var savePointRequestDto = new SavePointRequestDto(1L, 0L);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> pointService.savePoint(memberId, savePointRequestDto));
    }

    @Test
    @DisplayName("포인트 사용 시 0원이면 예외")
    void usePointZeroExceptionTest() {
        // given
        var memberId = 1L;
        var usePointRequestDto = new UsePointRequestDto(1L, 0L);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> pointService.usePoint(memberId, usePointRequestDto));
    }

    @Test
    @DisplayName("포인트 사용 시 적립 포인트보다 많으면 예외")
    void usePointMoreThenException() {
        // given
        var point1 = PointCreator.createPoint(1L, 1L, 10L, PointType.SAVE);
        var point2 = PointCreator.createPoint(1L, 1L, 10L, PointType.SAVE);
        pointRepository.save(point1);
        pointRepository.save(point2);

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> pointService.usePoint(1L, new UsePointRequestDto(1L, 30L)));
    }

    @Test
    @DisplayName("포인트 사용 취소 시 적립금이 맞지 않으면 예외")
    void cancelPointAmountTest() {
        // given
        var savePoint = PointCreator.createPoint(1L, 1L, 50L, PointType.SAVE);
        pointRepository.save(savePoint);

        var usePoint = new UsePointRequestDto(1L, 30L);
        pointService.usePoint(1L, usePoint);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                pointService.cancelPoint(1L, new CancelPointRequestDto(1L, 50L)));
    }
}
