package in.utkarshsingh.money.manager.entity;

import in.utkarshsingh.money.manager.enums.EventStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private String aggregateType; // PROFILE

    @Column(nullable = false)
    private String eventType; // PROFILE_ACTIVATION

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    private int retryCount;

    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
