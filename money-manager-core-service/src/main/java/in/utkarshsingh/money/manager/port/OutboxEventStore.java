package in.utkarshsingh.money.manager.port;

import in.utkarshsingh.money.manager.entity.OutboxEvent;
import in.utkarshsingh.money.manager.enums.EventStatus;

import java.util.List;

/**
 * Port for outbox persistence (DIP + ISP).
 * Single responsibility: load and persist outbox events.
 */
public interface OutboxEventStore {
    OutboxEvent save(OutboxEvent event);
    List<OutboxEvent> findTopByStatusOrderByCreatedAtAsc(EventStatus status, int limit);
}
