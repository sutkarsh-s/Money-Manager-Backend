package in.utkarshsingh.money.manager.adapter.messaging;

import in.utkarshsingh.money.manager.config.RabbitMQConfig;
import in.utkarshsingh.money.manager.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.port.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Adapter: publishes profile activation events to RabbitMQ (DIP).
 */
@Component
@RequiredArgsConstructor
public class RabbitEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishProfileActivation(ProfileActivationEvent event, String eventId) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PROFILE_ACTIVATION_QUEUE,
                event,
                message -> {
                    message.getMessageProperties().setHeader("eventId", eventId);
                    return message;
                }
        );
    }
}
