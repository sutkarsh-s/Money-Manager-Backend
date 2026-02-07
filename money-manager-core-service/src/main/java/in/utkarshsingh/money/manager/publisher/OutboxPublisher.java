package in.utkarshsingh.money.manager.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.utkarshsingh.money.manager.config.RabbitMQConfig;
import in.utkarshsingh.money.manager.entity.OutboxEvent;
import in.utkarshsingh.money.manager.enums.EventStatus;
import in.utkarshsingh.money.manager.event.ProfileActivationEvent;
import in.utkarshsingh.money.manager.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 30000)
    @Transactional
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(EventStatus.PENDING);

        for (OutboxEvent event : events) {
            try {
                ProfileActivationEvent payload =
                        objectMapper.readValue(event.getPayload(), ProfileActivationEvent.class);

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.PROFILE_ACTIVATION_QUEUE,
                        payload,
                        message -> {
                            message.getMessageProperties()
                                    .setHeader("eventId", event.getEventId());
                            return message;
                        }
                );

                event.setStatus(EventStatus.SENT);
                event.setProcessedAt(LocalDateTime.now());

                log.info("Event published: {}", event.getEventId());

            } catch (Exception ex) {

                event.setRetryCount(event.getRetryCount() + 1);

                if (event.getRetryCount() > 5) {
                    event.setStatus(EventStatus.FAILED);
                }

                log.error("Failed publishing event: {}", event.getEventId(), ex);
            }
        }
    }
}
