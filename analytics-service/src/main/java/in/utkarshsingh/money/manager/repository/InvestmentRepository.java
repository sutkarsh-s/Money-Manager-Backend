package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.InvestmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface InvestmentRepository extends JpaRepository<InvestmentEntity, Long> {

    @Query("SELECT COALESCE(SUM(i.currentValue), 0) FROM InvestmentEntity i WHERE i.profile.id = :profileId")
    BigDecimal findTotalCurrentValueByProfileId(@Param("profileId") Long profileId);
}
