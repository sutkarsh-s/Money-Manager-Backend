package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.DebtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface DebtRepository extends JpaRepository<DebtEntity, Long> {

    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM DebtEntity d WHERE d.profile.id = :profileId AND d.status = 'ACTIVE'")
    BigDecimal findTotalRemainingDebtByProfileId(@Param("profileId") Long profileId);
}
