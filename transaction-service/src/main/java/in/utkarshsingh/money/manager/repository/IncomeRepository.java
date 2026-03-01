package in.utkarshsingh.money.manager.repository;
import in.utkarshsingh.money.manager.entity.IncomeEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {
    @Modifying void deleteAllByProfileId(Long profileId);
    List<IncomeEntity> findByProfileIdOrderByDateDesc(Long profileId);
    List<IncomeEntity> findTop5ByProfileIdOrderByDateDesc(Long profileId);
    @Query("SELECT SUM(i.amount) FROM IncomeEntity i WHERE i.profile.id = :profileId")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);
    List<IncomeEntity> findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(Long profileId, LocalDate startDate, LocalDate endDate, String keyword, Sort sort);
    List<IncomeEntity> findByProfileIdAndDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);
    Optional<IncomeEntity> findByIdAndProfileId(Long id, Long profileId);
}
