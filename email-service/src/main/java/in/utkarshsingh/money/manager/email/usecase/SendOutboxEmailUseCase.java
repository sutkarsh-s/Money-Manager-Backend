package in.utkarshsingh.money.manager.email.usecase;

import in.utkarshsingh.money.manager.email.domain.EmailMessage;
import in.utkarshsingh.money.manager.email.entity.EmailOutbox;
import in.utkarshsingh.money.manager.email.enums.EmailStatus;
import in.utkarshsingh.money.manager.email.port.EmailOutboxStore;
import in.utkarshsingh.money.manager.email.port.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Use case: send a single outbox email by id and update status (SRP).
 * Depends on EmailSender and EmailOutboxStore ports (DIP).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SendOutboxEmailUseCase {

    private final EmailOutboxStore emailOutboxStore;
    private final EmailSender emailSender;

    @Transactional
    public void execute(Long outboxId) {
        EmailOutbox email = emailOutboxStore.findById(outboxId).orElse(null);
        if (email == null) {
            log.warn("Outbox entry not found: {}", outboxId);
            return;
        }
        if (email.getStatus() != EmailStatus.PENDING) {
            log.info("Outbox entry already processed: {}", outboxId);
            return;
        }

        try {
            emailSender.send(EmailMessage.builder()
                    .recipient(email.getRecipient())
                    .subject(email.getSubject())
                    .body(email.getBody())
                    .build());
            email.setStatus(EmailStatus.SENT);
            email.setSentAt(LocalDateTime.now());
            emailOutboxStore.save(email);
            log.info("Email sent successfully for outboxId={}", email.getId());
        } catch (Exception ex) {
            email.setStatus(EmailStatus.FAILED);
            emailOutboxStore.save(email);
            log.error("Email sending failed for outboxId={}", email.getId(), ex);
        }
    }
}
