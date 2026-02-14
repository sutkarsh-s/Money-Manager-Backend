package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.ContactMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessageEntity, Long> {
}
