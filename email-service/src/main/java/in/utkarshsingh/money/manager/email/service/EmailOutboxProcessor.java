package in.utkarshsingh.money.manager.email.service;

import in.utkarshsingh.money.manager.email.entity.EmailOutbox;
import in.utkarshsingh.money.manager.email.enums.EmailStatus;
import in.utkarshsingh.money.manager.email.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailOutboxProcessor {

    private final EmailOutboxRepository emailOutboxRepository;
    private final EmailService emailService;

    /**
     * ✅ Called by Spring Event (Immediate execution)
     */
    @Transactional
    public void processSingle(Long outboxId) {

        EmailOutbox email = emailOutboxRepository
                .findById(outboxId)
                .orElse(null);

        if (email == null) {
            log.warn("Outbox entry not found: {}", outboxId);
            return;
        }

        if (email.getStatus() != EmailStatus.PENDING) {
            log.info("Outbox entry already processed: {}", outboxId);
            return;
        }

        sendAndUpdateStatus(email);
    }

    /**
     * ✅ Scheduled fallback (Safety net), not needed , relying on each commit based even now
     */
// EmailOutboxProcessor.java
//    @Transactional
//    @Scheduled(initialDelay = 5000, fixedDelay = 60000)// 60 sec is fixed delay for emails
//    public void processPendingBatch() {
//
//        List<EmailOutbox> emails =
//                emailOutboxRepository
//                        .findTop10ByStatusOrderByCreatedAtAsc(EmailStatus.FAILED);
//
//        if (emails.isEmpty()) {
//            return;
//        }
//
//        log.info("Processing {} failed emails from outbox", emails.size());
//
//        for (EmailOutbox email : emails) {
//            sendAndUpdateStatus(email);
//        }
//    }

    /**
     * ✅ Common logic used by both methods
     */
    private void sendAndUpdateStatus(EmailOutbox email) {

        try {
            emailService.sendEmail(
                    email.getRecipient(),
                    email.getSubject(),
                    email.getBody()
            );

            email.setStatus(EmailStatus.SENT);
            email.setSentAt(LocalDateTime.now());
            emailOutboxRepository.save(email);
            log.info("Email sent successfully for outboxId={}", email.getId());

        } catch (Exception ex) {

            email.setStatus(EmailStatus.FAILED);

            log.error("Email sending failed for outboxId={}",
                    email.getId(), ex);
        }
    }
}
