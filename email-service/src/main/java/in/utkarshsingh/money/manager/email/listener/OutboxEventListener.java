package in.utkarshsingh.money.manager.email.listener;

import in.utkarshsingh.money.manager.email.event.OutboxCreatedEvent;
import in.utkarshsingh.money.manager.email.service.EmailOutboxProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OutboxEventListener {

    private final EmailOutboxProcessor processor;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOutboxCreated(OutboxCreatedEvent event) {
        processor.processSingle(event.outboxId());
    }
}

