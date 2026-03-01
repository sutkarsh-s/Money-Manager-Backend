package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.ExpenseDTO;
import in.utkarshsingh.money.manager.dto.IncomeDTO;
import in.utkarshsingh.money.manager.dto.RecentTransactionDTO;
import in.utkarshsingh.money.manager.dto.response.DashboardResponse;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.mapper.ExpenseMapper;
import in.utkarshsingh.money.manager.mapper.IncomeMapper;
import in.utkarshsingh.money.manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final InvestmentRepository investmentRepository;
    private final DebtRepository debtRepository;
    private final UserResolverService userResolverService;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboardData() {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        Long profileId = profile.getId();

        List<IncomeDTO> latestIncomes = incomeRepository.findTop5ByProfileIdOrderByDateDesc(profileId)
                .stream().map(incomeMapper::toDTO).toList();
        List<ExpenseDTO> latestExpenses = expenseRepository.findTop5ByProfileIdOrderByDateDesc(profileId)
                .stream().map(expenseMapper::toDTO).toList();
        List<RecentTransactionDTO> recentTransactions = buildRecentTransactions(profileId, latestIncomes, latestExpenses);

        BigDecimal totalIncome = incomeRepository.findTotalIncomeByProfileId(profileId);
        BigDecimal totalExpense = expenseRepository.findTotalExpenseByProfileId(profileId);
        BigDecimal totalSavings = savingsGoalRepository.findTotalSavedByProfileId(profileId);
        BigDecimal totalInvestments = investmentRepository.findTotalCurrentValueByProfileId(profileId);
        BigDecimal totalDebt = debtRepository.findTotalRemainingDebtByProfileId(profileId);

        totalIncome = totalIncome != null ? totalIncome : BigDecimal.ZERO;
        totalExpense = totalExpense != null ? totalExpense : BigDecimal.ZERO;
        totalSavings = totalSavings != null ? totalSavings : BigDecimal.ZERO;
        totalInvestments = totalInvestments != null ? totalInvestments : BigDecimal.ZERO;
        totalDebt = totalDebt != null ? totalDebt : BigDecimal.ZERO;

        BigDecimal cashBalance = totalIncome.subtract(totalExpense);
        BigDecimal netWorth = cashBalance.add(totalSavings).add(totalInvestments).subtract(totalDebt);

        return DashboardResponse.builder()
                .totalBalance(cashBalance)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalSavings(totalSavings)
                .totalInvestments(totalInvestments)
                .totalDebt(totalDebt)
                .netWorth(netWorth)
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
                        .id(i.getId()).profileId(profileId).icon(i.getIcon())
                        .name(i.getName()).amount(i.getAmount()).date(i.getDate())
                        .createdAt(i.getCreatedAt()).updatedAt(i.getUpdatedAt())
                        .type("income").build()),
                expenses.stream().map(e -> RecentTransactionDTO.builder()
                        .id(e.getId()).profileId(profileId).icon(e.getIcon())
                        .name(e.getName()).amount(e.getAmount()).date(e.getDate())
                        .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                        .type("expense").build())
        ).sorted(Comparator.comparing(RecentTransactionDTO::getDate)
                .thenComparing(RecentTransactionDTO::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed()
        ).toList();
    }
}
