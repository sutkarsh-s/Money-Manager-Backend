package in.utkarshsingh.money.manager.adapter.persistence;

import in.utkarshsingh.money.manager.entity.OutboxEvent;
import in.utkarshsingh.money.manager.enums.EventStatus;
import in.utkarshsingh.money.manager.port.OutboxEventStore;
import in.utkarshsingh.money.manager.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Adapter: implements OutboxEventStore using JPA repository (DIP).
 */
@Component
@RequiredArgsConstructor
public class OutboxEventStoreAdapter implements OutboxEventStore {

    private final OutboxRepository repository;

    @Override
    public OutboxEvent save(OutboxEvent event) {
        return repository.save(event);
    }

    @Override
    public List<OutboxEvent> findTopByStatusOrderByCreatedAtAsc(EventStatus status, int limit) {
        if (limit == 50) {
            return repository.findTop50ByStatusOrderByCreatedAtAsc(status);
        }
        return repository.findTop50ByStatusOrderByCreatedAtAsc(status).stream().limit(limit).toList();
    }
}
