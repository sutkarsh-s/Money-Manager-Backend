package in.utkarshsingh.money.manager.email.consumer;

import in.utkarshsingh.money.manager.email.config.RabbitMQConfig;
import in.utkarshsingh.money.manager.email.entity.EmailOutbox;
import in.utkarshsingh.money.manager.email.entity.ProcessedMessage;
import in.utkarshsingh.money.manager.email.enums.EmailStatus;
import in.utkarshsingh.money.manager.email.event.OutboxCreatedEvent;
import in.utkarshsingh.money.manager.email.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.email.repository.EmailOutboxRepository;
import in.utkarshsingh.money.manager.email.repository.ProcessedMessageRepository;
import in.utkarshsingh.money.manager.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProfileActivationEventConsumer {

    private final ProcessedMessageRepository processedMessageRepository;
    private final EmailOutboxRepository emailOutboxRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.activation.url}")
    private String activationUrl;

    @RabbitListener(queues = RabbitMQConfig.PROFILE_ACTIVATION_QUEUE)
    @Transactional
    public void consumeProfileActivationEvent(
            ProfileActivationEvent event,
            @Header("eventId") String eventId,
            Channel channel,
            Message message) throws IOException {

        log.info("Received activation event: {}", eventId);

        try {

            // Idempotency check
            if (processedMessageRepository.existsById(eventId)) {
                log.warn("Duplicate message ignored: {}", eventId);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            String activationLink =
                    activationUrl + "/api/v1.0/activate?token="
                            + event.getActivationToken();

            String subject = "Activate your Money Manager account";

            String body = "Hi " + event.getFullName() + ",\n\n"
                    + "Click the link below to activate your account:\n"
                    + activationLink + "\n\n"
                    + "If you did not register, please ignore this email.";

            // 🟢 Save email in OUTBOX (not send)
            EmailOutbox outbox = emailOutboxRepository.save(
                    EmailOutbox.builder()
                            .eventId(eventId)
                            .recipient(event.getEmail())
                            .subject(subject)
                            .body(body)
                            .status(EmailStatus.PENDING)
                            .createdAt(LocalDateTime.now())
                            .build()
            );

            //publishing event Instead of polling constantly in processPendingBatch in EmailOutboxProcessor,
            // used Spring event to Trigger processor after insert and still keep scheduled fallback
            eventPublisher.publishEvent(new OutboxCreatedEvent(outbox.getId()));

            // Save processed message
            processedMessageRepository.save(
                    ProcessedMessage.builder()
                            .eventId(eventId)
                            .processedAt(LocalDateTime.now())
                            .build()
            );

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            log.info("Activation event stored in outbox: {}", eventId);

        } catch (Exception ex) {

            log.error("Failed processing event: {}", eventId, ex);

            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
    }

}

