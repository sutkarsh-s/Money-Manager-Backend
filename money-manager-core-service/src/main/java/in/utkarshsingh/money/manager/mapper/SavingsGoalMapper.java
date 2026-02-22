package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.SavingsGoalDTO;
import in.utkarshsingh.money.manager.dto.request.SavingsGoalRequest;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.entity.SavingsGoalEntity;
import in.utkarshsingh.money.manager.enums.GoalStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class SavingsGoalMapper {

    public SavingsGoalEntity toEntity(SavingsGoalRequest request, ProfileEntity profile) {
        return SavingsGoalEntity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .status(GoalStatus.ACTIVE)
                .profile(profile)
                .build();
    }

    public SavingsGoalDTO toDTO(SavingsGoalEntity entity) {
        BigDecimal progress = BigDecimal.ZERO;
        if (entity.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            progress = entity.getCurrentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .divide(entity.getTargetAmount(), 2, RoundingMode.HALF_UP);
        }
        return SavingsGoalDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .targetAmount(entity.getTargetAmount())
                .currentAmount(entity.getCurrentAmount())
                .progressPercent(progress)
                .targetDate(entity.getTargetDate())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
