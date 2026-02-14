package in.utkarshsingh.money.manager.usecase;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.utkarshsingh.money.manager.entity.OutboxEvent;
import in.utkarshsingh.money.manager.enums.EventStatus;
import in.utkarshsingh.money.manager.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.port.EventPublisher;
import in.utkarshsingh.money.manager.port.OutboxEventStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Use case: publish pending outbox events to messaging (SRP).
 * Depends on ports only (DIP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PublishPendingOutboxEventsUseCase {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRIES = 5;

    private final OutboxEventStore outboxEventStore;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Transactional
    public void execute() {
        List<OutboxEvent> events = outboxEventStore.findTopByStatusOrderByCreatedAtAsc(EventStatus.PENDING, BATCH_SIZE);
        for (OutboxEvent event : events) {
            try {
                ProfileActivationEvent payload = objectMapper.readValue(event.getPayload(), ProfileActivationEvent.class);
                eventPublisher.publishProfileActivation(payload, event.getEventId());
                event.setStatus(EventStatus.SENT);
                event.setProcessedAt(LocalDateTime.now());
                outboxEventStore.save(event);
                log.info("Event published: {}", event.getEventId());
            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() > MAX_RETRIES) {
                    event.setStatus(EventStatus.FAILED);
                }
                outboxEventStore.save(event);
                log.error("Failed publishing event: {}", event.getEventId(), ex);
            }
        }
    }
}
