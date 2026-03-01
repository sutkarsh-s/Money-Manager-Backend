package in.utkarshsingh.money.manager.port;

import in.utkarshsingh.money.manager.enums.EventStatus;

import java.util.List;

public interface OutboxEventStore {
    Object save(Object event);
    List<?> findTopByStatusOrderByCreatedAtAsc(EventStatus status, int limit);
}
