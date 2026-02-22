package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.RecurringTransactionDTO;
import in.utkarshsingh.money.manager.dto.request.RecurringTransactionRequest;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.entity.RecurringTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class RecurringTransactionMapper {

    public RecurringTransactionEntity toEntity(RecurringTransactionRequest request,
                                               ProfileEntity profile,
                                               CategoryEntity category) {
        return RecurringTransactionEntity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .amount(request.getAmount())
                .type(request.getType())
                .frequency(request.getFrequency())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .nextExecutionDate(request.getStartDate())
                .isActive(true)
                .category(category)
                .profile(profile)
                .build();
    }

    public RecurringTransactionDTO toDTO(RecurringTransactionEntity entity) {
        return RecurringTransactionDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .amount(entity.getAmount())
                .type(entity.getType())
                .frequency(entity.getFrequency())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .nextExecutionDate(entity.getNextExecutionDate())
                .isActive(entity.getIsActive())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId() : null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
