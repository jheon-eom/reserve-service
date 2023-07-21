package com.example.PointService.domain.point.ticket;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByMemberId(Long memberId);

    void deleteByMemberId(Long memberId);
}
