package in.utkarshsingh.money.manager.email.consumer;

import in.utkarshsingh.money.manager.email.config.RabbitMQConfig;
import in.utkarshsingh.money.manager.email.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.email.usecase.HandleProfileActivationEventUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import java.io.IOException;

/**
 * Inbound adapter: receives RabbitMQ messages and delegates to use case (SRP).
 * Only responsible for deserialization, ack/reject, and calling use case.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileActivationEventConsumer {

    private final HandleProfileActivationEventUseCase handleProfileActivationEventUseCase;

    @RabbitListener(queues = RabbitMQConfig.PROFILE_ACTIVATION_QUEUE)
    public void consumeProfileActivationEvent(
            ProfileActivationEvent event,
            @Header("eventId") String eventId,
            Channel channel,
            Message message) throws IOException {

        log.info("Received activation event: {}", eventId);

        try {
            handleProfileActivationEventUseCase.execute(event, eventId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception ex) {
            log.error("Failed processing event: {}", eventId, ex);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
