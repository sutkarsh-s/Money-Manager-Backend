package in.utkarshsingh.money.manager.email.listener;

import in.utkarshsingh.money.manager.email.event.OutboxCreatedEvent;
import in.utkarshsingh.money.manager.email.usecase.SendOutboxEmailUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Inbound adapter: listens for OutboxCreatedEvent and triggers send use case (SRP).
 */
@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final SendOutboxEmailUseCase sendOutboxEmailUseCase;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCreated(OutboxCreatedEvent event) {
        sendOutboxEmailUseCase.execute(event.outboxId());
    }
}
