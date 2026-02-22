package in.utkarshsingh.money.manager.dto;

import in.utkarshsingh.money.manager.enums.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SavingsGoalDTO {

    private Long id;
    private String name;
    private String icon;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal progressPercent;
    private LocalDate targetDate;
    private GoalStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
