package in.utkarshsingh.money.manager.email.repository;

import in.utkarshsingh.money.manager.email.entity.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository
        extends JpaRepository<ProcessedMessage, String> {
}

