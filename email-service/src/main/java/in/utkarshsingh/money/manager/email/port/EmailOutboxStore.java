package in.utkarshsingh.money.manager.email.port;

import in.utkarshsingh.money.manager.email.entity.EmailOutbox;
import in.utkarshsingh.money.manager.email.enums.EmailStatus;

import java.util.Optional;

/**
 * Port for persisting and loading email outbox entries (DIP + ISP).
 * Single responsibility: outbox persistence only.
 */
public interface EmailOutboxStore {
    EmailOutbox save(EmailOutbox outbox);
    Optional<EmailOutbox> findById(Long id);
}
