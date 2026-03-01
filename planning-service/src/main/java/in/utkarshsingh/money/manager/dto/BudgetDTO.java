package in.utkarshsingh.money.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetDTO {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private String categoryIcon;
    private BigDecimal amount;
    private BigDecimal spent;
    private BigDecimal remaining;
    private String budgetMonth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
