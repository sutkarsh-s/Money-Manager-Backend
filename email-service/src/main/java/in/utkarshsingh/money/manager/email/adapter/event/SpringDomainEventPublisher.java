package in.utkarshsingh.money.manager.email.adapter.event;

import in.utkarshsingh.money.manager.email.event.OutboxCreatedEvent;
import in.utkarshsingh.money.manager.email.port.DomainEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Adapter: publishes domain events via Spring's ApplicationEventPublisher (DIP).
 */
@Component
@RequiredArgsConstructor
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publish(OutboxCreatedEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
