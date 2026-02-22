package in.utkarshsingh.money.manager.dto.response;

import in.utkarshsingh.money.manager.dto.ExpenseDTO;
import in.utkarshsingh.money.manager.dto.IncomeDTO;
import in.utkarshsingh.money.manager.dto.RecentTransactionDTO;
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
public class DashboardResponse {

    private BigDecimal totalBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private List<IncomeDTO> recent5Incomes;
    private List<ExpenseDTO> recent5Expenses;
    private List<RecentTransactionDTO> recentTransactions;
}
