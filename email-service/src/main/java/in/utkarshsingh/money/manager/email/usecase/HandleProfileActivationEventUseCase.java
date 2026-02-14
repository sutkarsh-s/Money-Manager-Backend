package in.utkarshsingh.money.manager.email.usecase;

import in.utkarshsingh.money.manager.email.domain.ActivationEmailContentBuilder;
import in.utkarshsingh.money.manager.email.domain.EmailMessage;
import in.utkarshsingh.money.manager.email.entity.EmailOutbox;
import in.utkarshsingh.money.manager.email.enums.EmailStatus;
import in.utkarshsingh.money.manager.email.event.OutboxCreatedEvent;
import in.utkarshsingh.money.manager.email.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.email.port.DomainEventPublisher;
import in.utkarshsingh.money.manager.email.port.EmailOutboxStore;
import in.utkarshsingh.money.manager.email.port.ProcessedMessageStore;
import in.utkarshsingh.money.manager.email.entity.ProcessedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use case: handle incoming profile activation event (idempotent, persist outbox, publish event).
 * SRP: Orchestrates only this flow. Depends on ports (DIP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HandleProfileActivationEventUseCase {

    private final ProcessedMessageStore processedMessageStore;
    private final EmailOutboxStore emailOutboxStore;
    private final DomainEventPublisher domainEventPublisher;
    private final ActivationEmailContentBuilder activationEmailContentBuilder;

    /**
     * @return true if event was processed (stored in outbox), false if duplicate (idempotent skip).
     */
    @Transactional
    public boolean execute(ProfileActivationEvent event, String eventId) {
        if (processedMessageStore.existsByEventId(eventId)) {
            log.warn("Duplicate message ignored: {}", eventId);
            return false;
        }

        EmailMessage message = activationEmailContentBuilder.build(event);
        EmailOutbox outbox = EmailOutbox.builder()
                .eventId(eventId)
                .recipient(message.getRecipient())
                .subject(message.getSubject())
                .body(message.getBody())
                .status(EmailStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        outbox = emailOutboxStore.save(outbox);

        domainEventPublisher.publish(new OutboxCreatedEvent(outbox.getId()));
        processedMessageStore.save(
                ProcessedMessage.builder()
                        .eventId(eventId)
                        .processedAt(LocalDateTime.now())
                        .build()
        );
        log.info("Activation event stored in outbox: {}", eventId);
        return true;
    }
}
