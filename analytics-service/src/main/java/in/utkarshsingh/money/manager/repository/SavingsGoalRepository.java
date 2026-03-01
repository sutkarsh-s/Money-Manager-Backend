package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.SavingsGoalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoalEntity, Long> {

    @Query("SELECT COALESCE(SUM(s.currentAmount), 0) FROM SavingsGoalEntity s WHERE s.profile.id = :profileId AND s.status = 'ACTIVE'")
    BigDecimal findTotalSavedByProfileId(@Param("profileId") Long profileId);
}
