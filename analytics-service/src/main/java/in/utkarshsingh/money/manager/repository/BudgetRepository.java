package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.BudgetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<BudgetEntity, Long> {

    List<BudgetEntity> findByProfileIdAndBudgetMonth(Long profileId, String budgetMonth);
}
