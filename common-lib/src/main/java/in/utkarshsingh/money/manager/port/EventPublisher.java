package in.utkarshsingh.money.manager.port;

import in.utkarshsingh.money.manager.event.ProfileActivationEvent;

public interface EventPublisher {
    void publishProfileActivation(ProfileActivationEvent event, String eventId);
}
