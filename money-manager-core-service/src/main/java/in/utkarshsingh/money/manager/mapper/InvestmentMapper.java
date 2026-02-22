package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.InvestmentDTO;
import in.utkarshsingh.money.manager.dto.request.InvestmentRequest;
import in.utkarshsingh.money.manager.entity.InvestmentEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class InvestmentMapper {

    public InvestmentEntity toEntity(InvestmentRequest request, ProfileEntity profile) {
        return InvestmentEntity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .type(request.getType())
                .investedAmount(request.getInvestedAmount())
                .currentValue(request.getCurrentValue())
                .purchaseDate(request.getPurchaseDate())
                .notes(request.getNotes())
                .profile(profile)
                .build();
    }

    public InvestmentDTO toDTO(InvestmentEntity entity) {
        BigDecimal gainLoss = entity.getCurrentValue().subtract(entity.getInvestedAmount());
        BigDecimal gainLossPercent = BigDecimal.ZERO;
        if (entity.getInvestedAmount().compareTo(BigDecimal.ZERO) > 0) {
            gainLossPercent = gainLoss
                    .multiply(BigDecimal.valueOf(100))
                    .divide(entity.getInvestedAmount(), 2, RoundingMode.HALF_UP);
        }
        return InvestmentDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .type(entity.getType())
                .investedAmount(entity.getInvestedAmount())
                .currentValue(entity.getCurrentValue())
                .gainLoss(gainLoss)
                .gainLossPercent(gainLossPercent)
                .purchaseDate(entity.getPurchaseDate())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
