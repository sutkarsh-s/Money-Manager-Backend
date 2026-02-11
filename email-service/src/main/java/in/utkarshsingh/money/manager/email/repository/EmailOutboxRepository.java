package in.utkarshsingh.money.manager.email.repository;

import in.utkarshsingh.money.manager.email.entity.EmailOutbox;
import in.utkarshsingh.money.manager.email.enums.EmailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmailOutboxRepository
        extends JpaRepository<EmailOutbox, Long> {

    List<EmailOutbox> findTop10ByStatusOrderByCreatedAtAsc(
            EmailStatus status);
}

