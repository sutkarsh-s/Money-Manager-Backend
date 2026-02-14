package in.utkarshsingh.money.manager.email.port;

import in.utkarshsingh.money.manager.email.entity.ProcessedMessage;

/**
 * Port for idempotency: record which messages have been processed (DIP + ISP).
 */
public interface ProcessedMessageStore {
    boolean existsByEventId(String eventId);
    ProcessedMessage save(ProcessedMessage message);
}
