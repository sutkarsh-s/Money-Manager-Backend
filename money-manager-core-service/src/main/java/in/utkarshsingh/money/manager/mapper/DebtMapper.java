package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.DebtDTO;
import in.utkarshsingh.money.manager.dto.request.DebtRequest;
import in.utkarshsingh.money.manager.entity.DebtEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.enums.DebtStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class DebtMapper {

    public DebtEntity toEntity(DebtRequest request, ProfileEntity profile) {
        return DebtEntity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .type(request.getType())
                .originalAmount(request.getOriginalAmount())
                .remainingAmount(request.getOriginalAmount())
                .interestRate(request.getInterestRate())
                .emiAmount(request.getEmiAmount())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(DebtStatus.ACTIVE)
                .notes(request.getNotes())
                .profile(profile)
                .build();
    }

    public DebtDTO toDTO(DebtEntity entity) {
        BigDecimal paid = entity.getOriginalAmount().subtract(entity.getRemainingAmount());
        BigDecimal progress = BigDecimal.ZERO;
        if (entity.getOriginalAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = paid
                    .multiply(BigDecimal.valueOf(100))
                    .divide(entity.getOriginalAmount(), 2, RoundingMode.HALF_UP);
        }
        return DebtDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .type(entity.getType())
                .originalAmount(entity.getOriginalAmount())
                .remainingAmount(entity.getRemainingAmount())
                .paidAmount(paid)
                .interestRate(entity.getInterestRate())
                .emiAmount(entity.getEmiAmount())
                .progressPercent(progress)
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
