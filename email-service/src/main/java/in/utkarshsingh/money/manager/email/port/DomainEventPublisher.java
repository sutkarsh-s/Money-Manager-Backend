package in.utkarshsingh.money.manager.email.port;

import in.utkarshsingh.money.manager.email.event.OutboxCreatedEvent;

/**
 * Port for publishing domain events (DIP).
 * Implementation can use Spring ApplicationEventPublisher or other bus.
 */
public interface DomainEventPublisher {
    void publish(OutboxCreatedEvent event);
}
