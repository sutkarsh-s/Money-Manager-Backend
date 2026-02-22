package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.RecurringTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransactionEntity, Long> {

    @Modifying
    void deleteAllByProfileId(Long profileId);

    List<RecurringTransactionEntity> findByProfileIdOrderByCreatedAtDesc(Long profileId);

    Optional<RecurringTransactionEntity> findByIdAndProfileId(Long id, Long profileId);

    List<RecurringTransactionEntity> findByIsActiveTrueAndNextExecutionDateLessThanEqual(LocalDate date);
}
