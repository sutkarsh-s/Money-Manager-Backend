package in.utkarshsingh.money.manager.email.adapter.persistence;

import in.utkarshsingh.money.manager.email.entity.EmailOutbox;
import in.utkarshsingh.money.manager.email.port.EmailOutboxStore;
import in.utkarshsingh.money.manager.email.repository.EmailOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adapter: implements EmailOutboxStore port using JPA repository (DIP).
 */
@Component
@RequiredArgsConstructor
public class EmailOutboxStoreAdapter implements EmailOutboxStore {

    private final EmailOutboxRepository repository;

    @Override
    public EmailOutbox save(EmailOutbox outbox) {
        return repository.save(outbox);
    }

    @Override
    public Optional<EmailOutbox> findById(Long id) {
        return repository.findById(id);
    }
}
