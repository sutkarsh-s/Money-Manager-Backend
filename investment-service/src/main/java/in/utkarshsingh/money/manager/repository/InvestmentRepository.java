package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.InvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InvestmentRepository extends JpaRepository<InvestmentEntity, Long> {

    @Modifying
    void deleteAllByProfileId(Long profileId);

    List<InvestmentEntity> findByProfileIdOrderByCreatedAtDesc(Long profileId);

    Optional<InvestmentEntity> findByIdAndProfileId(Long id, Long profileId);

    @Query("SELECT COALESCE(SUM(i.investedAmount), 0) FROM InvestmentEntity i WHERE i.profile.id = :profileId")
    BigDecimal findTotalInvestedByProfileId(@Param("profileId") Long profileId);

    @Query("SELECT COALESCE(SUM(i.currentValue), 0) FROM InvestmentEntity i WHERE i.profile.id = :profileId")
    BigDecimal findTotalCurrentValueByProfileId(@Param("profileId") Long profileId);
}
