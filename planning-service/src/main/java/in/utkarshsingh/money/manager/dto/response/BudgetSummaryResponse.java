package in.utkarshsingh.money.manager.dto.response;

import in.utkarshsingh.money.manager.dto.BudgetDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BudgetSummaryResponse {

    private String budgetMonth;
    private BigDecimal totalBudgeted;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private List<BudgetDTO> budgets;
}
