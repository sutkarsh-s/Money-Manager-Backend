package in.utkarshsingh.money.manager.email.adapter.persistence;

import in.utkarshsingh.money.manager.email.entity.ProcessedMessage;
import in.utkarshsingh.money.manager.email.port.ProcessedMessageStore;
import in.utkarshsingh.money.manager.email.repository.ProcessedMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter: implements ProcessedMessageStore port using JPA repository (DIP).
 */
@Component
@RequiredArgsConstructor
public class ProcessedMessageStoreAdapter implements ProcessedMessageStore {

    private final ProcessedMessageRepository repository;

    @Override
    public boolean existsByEventId(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public ProcessedMessage save(ProcessedMessage message) {
        return repository.save(message);
    }
}
