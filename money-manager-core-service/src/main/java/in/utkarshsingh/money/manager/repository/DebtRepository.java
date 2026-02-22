package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.DebtEntity;
import in.utkarshsingh.money.manager.enums.DebtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.Modifying;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DebtRepository extends JpaRepository<DebtEntity, Long> {

    @Modifying
    void deleteAllByProfileId(Long profileId);

    List<DebtEntity> findByProfileIdOrderByCreatedAtDesc(Long profileId);

    Optional<DebtEntity> findByIdAndProfileId(Long id, Long profileId);

    List<DebtEntity> findByProfileIdAndStatus(Long profileId, DebtStatus status);

    @Query("SELECT COALESCE(SUM(d.remainingAmount), 0) FROM DebtEntity d WHERE d.profile.id = :profileId AND d.status = 'ACTIVE'")
    BigDecimal findTotalRemainingDebtByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT COALESCE(SUM(d.emiAmount), 0) FROM DebtEntity d WHERE d.profile.id = :profileId AND d.status = 'ACTIVE' AND d.emiAmount IS NOT NULL")
    BigDecimal findTotalMonthlyEmiByProfileId(@Param("profileId") Long profileId);
}
