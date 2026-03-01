package in.utkarshsingh.money.manager.dto;

import in.utkarshsingh.money.manager.enums.InvestmentType;
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
public class InvestmentDTO {

    private Long id;
    private String name;
    private String icon;
    private InvestmentType type;
    private BigDecimal investedAmount;
    private BigDecimal currentValue;
    private BigDecimal gainLoss;
    private BigDecimal gainLossPercent;
    private LocalDate purchaseDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
