package com.codeus.outbox.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "outbox")
@EqualsAndHashCode(of = "id")
public class OutboxEvent {

    @Id
    @GeneratedValue
    @Column(nullable = false, unique = true, updatable = false)
    private UUID id;

    // new fields which describe the event
    private UUID aggregateId;
    private String aggregateType;
    private String eventType;

    // payload - JSON
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxEventStatus status;

    // other fields

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant processedAt;
}
