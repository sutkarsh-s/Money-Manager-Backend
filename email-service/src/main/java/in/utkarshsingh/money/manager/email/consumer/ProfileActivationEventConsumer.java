package in.utkarshsingh.money.manager.email.consumer;

import in.utkarshsingh.money.manager.email.config.RabbitMQConfig;
import in.utkarshsingh.money.manager.email.entity.ProcessedMessage;
import in.utkarshsingh.money.manager.email.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.email.repository.ProcessedMessageRepository;
import in.utkarshsingh.money.manager.email.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
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

    private final EmailService emailService;
    private final ProcessedMessageRepository processedMessageRepository;

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

            // 🔐 Idempotency Check
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

            emailService.sendEmail(event.getEmail(), subject, body);

            // Save processed message (same transaction)
            processedMessageRepository.save(
                    ProcessedMessage.builder()
                            .eventId(eventId)
                            .processedAt(LocalDateTime.now())
                            .build()
            );

            // ACK only after DB + Email success
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            log.info("Activation email processed successfully: {}", eventId);

        } catch (Exception ex) {

            log.error("Failed processing event: {}", eventId, ex);

            // Reject and send to DLQ
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}

