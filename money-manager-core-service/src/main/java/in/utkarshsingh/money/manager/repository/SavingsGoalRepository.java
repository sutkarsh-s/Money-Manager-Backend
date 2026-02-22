package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.SavingsGoalEntity;
import in.utkarshsingh.money.manager.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, Long> {

    @Modifying
    void deleteAllByProfileId(Long profileId);

    List<SavingsGoalEntity> findByProfileIdOrderByCreatedAtDesc(Long profileId);

    Optional<SavingsGoalEntity> findByIdAndProfileId(Long id, Long profileId);

    List<SavingsGoalEntity> findByProfileIdAndStatus(Long profileId, GoalStatus status);

    @Query("SELECT COALESCE(SUM(s.currentAmount), 0) FROM SavingsGoalEntity s WHERE s.profile.id = :profileId AND s.status = 'ACTIVE'")
    BigDecimal findTotalSavedByProfileId(@Param("profileId") Long profileId);
}
