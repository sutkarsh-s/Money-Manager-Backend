package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.BudgetDTO;
import in.utkarshsingh.money.manager.dto.request.BudgetRequest;
import in.utkarshsingh.money.manager.entity.BudgetEntity;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class BudgetMapper {

    public BudgetEntity toEntity(BudgetRequest request, ProfileEntity profile, CategoryEntity category) {
        return BudgetEntity.builder()
                .amount(request.getAmount())
                .budgetMonth(request.getBudgetMonth())
                .category(category)
                .profile(profile)
                .build();
    }

    public BudgetDTO toDTO(BudgetEntity entity, BigDecimal spent) {
        BigDecimal remaining = entity.getAmount().subtract(spent);
        return BudgetDTO.builder()
                .id(entity.getId())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .categoryIcon(entity.getCategory() != null ? entity.getCategory().getIcon() : null)
                .amount(entity.getAmount())
                .spent(spent)
                .remaining(remaining)
                .budgetMonth(entity.getBudgetMonth())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
