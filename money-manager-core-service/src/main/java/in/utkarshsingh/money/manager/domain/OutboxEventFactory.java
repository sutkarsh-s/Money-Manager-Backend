package in.utkarshsingh.money.manager.domain;

import in.utkarshsingh.money.manager.entity.OutboxEvent;
import in.utkarshsingh.money.manager.enums.EventStatus;
import in.utkarshsingh.money.manager.event.ProfileActivationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * SRP: Creates outbox event from profile activation (single responsibility).
 * OCP: New event types can be added via new factory methods or strategies without changing this.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventFactory {

    public OutboxEvent createProfileActivationOutboxEvent(ProfileActivationEvent event, String payloadJson) {
        return OutboxEvent.builder()
                .eventId(event.getEventId())
                .aggregateType("PROFILE")
                .eventType("PROFILE_ACTIVATION")
                .payload(payloadJson)
                .status(EventStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
