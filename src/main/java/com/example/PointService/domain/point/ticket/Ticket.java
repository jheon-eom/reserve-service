package com.example.PointService.domain.point.ticket;

import jakarta.persistence.*;

@Entity
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long memberId;

    public Ticket(Long memberId) {
        this.memberId = memberId;
    }
}
