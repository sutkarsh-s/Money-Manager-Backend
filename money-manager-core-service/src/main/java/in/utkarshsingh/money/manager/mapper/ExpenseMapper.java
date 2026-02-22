package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.ExpenseDTO;
import in.utkarshsingh.money.manager.dto.request.ExpenseRequest;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.ExpenseEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class ExpenseMapper {

    public ExpenseEntity toEntity(ExpenseRequest request, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .amount(request.getAmount())
                .date(request.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    public ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
