package com.example.PointService.application;

import com.example.PointService.domain.point.dto.*;
import com.example.PointService.domain.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1")
@RestController
public class PointApiController {
    private final PointService pointService;

    @GetMapping("/member/{id}/point")
    public ResponseEntity<PointDto> getPoint(@PathVariable(value = "id") Long memberId) {

        return ResponseEntity.ok(pointService.getPoint(memberId));
    }

    @GetMapping("/member/{id}/point/history")
    public ResponseEntity<Page<PointHistoryDto>> getPointHistory(
            @PathVariable(value = "id") Long memberId,
            @RequestParam Integer page,
            @RequestParam Integer size) {

        return ResponseEntity.ok(pointService.getPointHistory(memberId, PageRequest.of(page, size)));
    }

    @PostMapping("/member/{id}/point/save")
    public ResponseEntity<PointDto> savePoint(@PathVariable(value = "id") Long memberId,
                                              @RequestBody SavePointRequestDto savePointRequestDto) {

        return ResponseEntity.ok(pointService.savePoint(memberId, savePointRequestDto));
    }

    @PostMapping("/member/{id}/point/use")
    public ResponseEntity<PointDto> usePoint(@PathVariable(value = "id") Long memberId,
                                             @RequestBody UsePointRequestDto usePointRequestDto) {

        return ResponseEntity.ok(pointService.usePoint(memberId, usePointRequestDto));
    }

    @PostMapping("/member/{memberId}/point/cancel")
    public ResponseEntity<PointDto> cancelPoint(@PathVariable(value = "memberId") Long memberId,
                                                @RequestBody CancelPointRequestDto cancelPointRequestDto) {

        return ResponseEntity.ok(pointService.cancelPoint(memberId, cancelPointRequestDto));
    }
}
