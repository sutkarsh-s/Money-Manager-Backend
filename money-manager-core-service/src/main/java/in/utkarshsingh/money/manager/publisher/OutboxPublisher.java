package in.utkarshsingh.money.manager.publisher;

import in.utkarshsingh.money.manager.usecase.PublishPendingOutboxEventsUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler: triggers publish of pending outbox events (SRP).
 * Delegates to use case; no business logic here.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class OutboxPublisher {

    private final PublishPendingOutboxEventsUseCase publishPendingOutboxEventsUseCase;

    @Scheduled(fixedDelay = 30000)
    public void publishPendingEvents() {
        publishPendingOutboxEventsUseCase.execute();
    }
}
