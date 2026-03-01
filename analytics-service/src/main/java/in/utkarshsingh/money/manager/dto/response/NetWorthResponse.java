package in.utkarshsingh.money.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NetWorthResponse {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal cashBalance;
    private BigDecimal totalSavings;
    private BigDecimal totalInvestments;
    private BigDecimal totalDebt;
    private BigDecimal netWorth;
}
