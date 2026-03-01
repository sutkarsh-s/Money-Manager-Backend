package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.IncomeDTO;
import in.utkarshsingh.money.manager.entity.IncomeEntity;
import org.springframework.stereotype.Component;

@Component
public class IncomeMapper {

    public IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
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
