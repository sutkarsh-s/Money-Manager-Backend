package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.ExpenseDTO;
import in.utkarshsingh.money.manager.dto.IncomeDTO;
import in.utkarshsingh.money.manager.dto.RecentTransactionDTO;
import in.utkarshsingh.money.manager.dto.response.DashboardResponse;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final ProfileService profileService;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        ProfileEntity profile = profileService.getCurrentProfile();

        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();

        List<RecentTransactionDTO> recentTransactions = buildRecentTransactions(profile.getId(), latestIncomes, latestExpenses);

        return DashboardResponse.builder()
                .totalBalance(incomeService.getTotalIncomeForCurrentUser()
                        .subtract(expenseService.getTotalExpenseForCurrentUser()))
                .totalIncome(incomeService.getTotalIncomeForCurrentUser())
                .totalExpense(expenseService.getTotalExpenseForCurrentUser())
                .recent5Incomes(latestIncomes)
                .recent5Expenses(latestExpenses)
                .recentTransactions(recentTransactions)
                .build();
    }

    private List<RecentTransactionDTO> buildRecentTransactions(Long profileId,
                                                               List<IncomeDTO> incomes,
                                                               List<ExpenseDTO> expenses) {
        return Stream.concat(
                incomes.stream().map(i -> RecentTransactionDTO.builder()
                        .id(i.getId())
                        .profileId(profileId)
                        .icon(i.getIcon())
                        .name(i.getName())
                        .amount(i.getAmount())
                        .date(i.getDate())
                        .createdAt(i.getCreatedAt())
                        .updatedAt(i.getUpdatedAt())
                        .type("income")
                        .build()),
                expenses.stream().map(e -> RecentTransactionDTO.builder()
                        .id(e.getId())
                        .profileId(profileId)
                        .icon(e.getIcon())
                        .name(e.getName())
                        .amount(e.getAmount())
                        .date(e.getDate())
                        .createdAt(e.getCreatedAt())
                        .updatedAt(e.getUpdatedAt())
                        .type("expense")
                        .build())
        ).sorted(Comparator.comparing(RecentTransactionDTO::getDate)
                .thenComparing(RecentTransactionDTO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
        ).toList();
    }
}
