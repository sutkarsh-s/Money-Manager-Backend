package in.utkarshsingh.money.manager.port;

import in.utkarshsingh.money.manager.event.ProfileActivationEvent;

/**
 * Port for publishing domain events to messaging (DIP + ISP).
 * Implementations: RabbitMQ, Kafka, etc.
 */
public interface EventPublisher {
    void publishProfileActivation(ProfileActivationEvent event, String eventId);
}
