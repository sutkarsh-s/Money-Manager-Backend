package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    @Modifying
    void deleteAllByProfileId(Long profileId);

    List<BudgetEntity> findByProfileIdAndBudgetMonth(Long profileId, String budgetMonth);

    Optional<BudgetEntity> findByIdAndProfileId(Long id, Long profileId);

    boolean existsByProfileIdAndCategoryIdAndBudgetMonth(Long profileId, Long categoryId, String budgetMonth);
}
