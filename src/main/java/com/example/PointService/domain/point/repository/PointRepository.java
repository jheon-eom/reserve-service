package com.example.PointService.domain.point.repository;

import com.example.PointService.domain.point.entity.Point;
import com.example.PointService.domain.point.entity.PointType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointRepository extends JpaRepository<Point, Long> {

    List<Point> findAllByMemberId(Long memberId);

    List<Point> findAllByTrxIdAndType(Long aLong, PointType type);

    Page<Point> findAllPageByMemberId(Long memberId, Pageable pageable);
}
