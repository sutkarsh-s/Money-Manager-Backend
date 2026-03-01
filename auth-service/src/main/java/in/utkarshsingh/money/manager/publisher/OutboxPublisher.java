package in.utkarshsingh.money.manager.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.utkarshsingh.money.manager.entity.OutboxEvent;
import in.utkarshsingh.money.manager.enums.EventStatus;
import in.utkarshsingh.money.manager.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.port.EventPublisher;
import in.utkarshsingh.money.manager.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private static final int MAX_RETRIES = 5;

    private final OutboxRepository outboxRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(EventStatus.PENDING);
        for (OutboxEvent event : events) {
            try {
                ProfileActivationEvent payload = objectMapper.readValue(event.getPayload(), ProfileActivationEvent.class);
                eventPublisher.publishProfileActivation(payload, event.getEventId());
                event.setStatus(EventStatus.SENT);
                event.setProcessedAt(LocalDateTime.now());
                outboxRepository.save(event);
            } catch (Exception ex) {
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() > MAX_RETRIES) {
                    event.setStatus(EventStatus.FAILED);
                }
                outboxRepository.save(event);
                log.error("Failed publishing event: {}", event.getEventId(), ex);
            }
        }
    }
}
