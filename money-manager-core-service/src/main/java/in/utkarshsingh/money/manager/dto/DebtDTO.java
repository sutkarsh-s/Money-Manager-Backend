package in.utkarshsingh.money.manager.dto;

import in.utkarshsingh.money.manager.enums.DebtStatus;
import in.utkarshsingh.money.manager.enums.DebtType;
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
public class DebtDTO {

    private Long id;
    private String name;
    private String icon;
    private DebtType type;
    private BigDecimal originalAmount;
    private BigDecimal remainingAmount;
    private BigDecimal paidAmount;
    private BigDecimal interestRate;
    private BigDecimal emiAmount;
    private BigDecimal progressPercent;
    private LocalDate startDate;
    private LocalDate endDate;
    private DebtStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
