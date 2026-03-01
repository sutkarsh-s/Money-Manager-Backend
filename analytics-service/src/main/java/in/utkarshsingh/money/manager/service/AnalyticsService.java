package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.ExpenseDTO;
import in.utkarshsingh.money.manager.dto.IncomeDTO;
import in.utkarshsingh.money.manager.dto.response.BudgetSummaryResponse;
import in.utkarshsingh.money.manager.dto.response.CategoryBreakdownResponse;
import in.utkarshsingh.money.manager.dto.response.MonthlySummaryResponse;
import in.utkarshsingh.money.manager.dto.response.NetWorthResponse;
import in.utkarshsingh.money.manager.dto.BudgetDTO;
import in.utkarshsingh.money.manager.entity.BudgetEntity;
import in.utkarshsingh.money.manager.entity.ExpenseEntity;
import in.utkarshsingh.money.manager.entity.IncomeEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.mapper.BudgetMapper;
import in.utkarshsingh.money.manager.mapper.ExpenseMapper;
import in.utkarshsingh.money.manager.mapper.IncomeMapper;
import in.utkarshsingh.money.manager.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final InvestmentRepository investmentRepository;
    private final DebtRepository debtRepository;
    private final BudgetRepository budgetRepository;
    private final UserResolverService userResolverService;
    private final IncomeMapper incomeMapper;
    private final ExpenseMapper expenseMapper;
    private final BudgetMapper budgetMapper;

    @Transactional(readOnly = true)
    public NetWorthResponse getNetWorth() {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        Long profileId = profile.getId();

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

        return NetWorthResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .cashBalance(cashBalance)
                .totalSavings(totalSavings)
                .totalInvestments(totalInvestments)
                .totalDebt(totalDebt)
                .netWorth(netWorth)
                .build();
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(int year) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        List<MonthlySummaryResponse.MonthData> months = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), start, end);
            List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), start, end);

            BigDecimal income = incomes.stream().map(IncomeEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expense = expenses.stream().map(ExpenseEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

            months.add(MonthlySummaryResponse.MonthData.builder()
                    .month(m)
                    .monthName(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .income(income)
                    .expense(expense)
                    .savings(income.subtract(expense))
                    .build());
        }

        return MonthlySummaryResponse.builder()
                .year(year)
                .months(months)
                .build();
    }

    @Transactional(readOnly = true)
    public CategoryBreakdownResponse getCategoryBreakdown(String month, String type) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        String effectiveMonth = month != null ? month : YearMonth.now().toString();
        YearMonth ym = YearMonth.parse(effectiveMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        if ("income".equalsIgnoreCase(type)) {
            return buildIncomeBreakdown(profile.getId(), start, end, effectiveMonth);
        } else {
            return buildExpenseBreakdown(profile.getId(), start, end, effectiveMonth);
        }
    }

    private CategoryBreakdownResponse buildExpenseBreakdown(Long profileId, LocalDate start, LocalDate end, String month) {
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profileId, start, end);
        BigDecimal total = expenses.stream().map(ExpenseEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, List<ExpenseEntity>> grouped = expenses.stream()
                .filter(e -> e.getCategory() != null)
                .collect(Collectors.groupingBy(e -> e.getCategory().getId()));

        List<CategoryBreakdownResponse.CategoryData> categories = grouped.entrySet().stream()
                .map(entry -> {
                    ExpenseEntity sample = entry.getValue().get(0);
                    BigDecimal amount = entry.getValue().stream().map(ExpenseEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal pct = total.compareTo(BigDecimal.ZERO) > 0
                            ? amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return CategoryBreakdownResponse.CategoryData.builder()
                            .categoryId(sample.getCategory().getId())
                            .categoryName(sample.getCategory().getName())
                            .categoryIcon(sample.getCategory().getIcon())
                            .amount(amount)
                            .percentage(pct)
                            .build();
                })
                .sorted(Comparator.comparing(CategoryBreakdownResponse.CategoryData::getAmount).reversed())
                .toList();

        return CategoryBreakdownResponse.builder()
                .month(month).type("expense").total(total).categories(categories).build();
    }

    private CategoryBreakdownResponse buildIncomeBreakdown(Long profileId, LocalDate start, LocalDate end, String month) {
        List<IncomeEntity> incomes = incomeRepository.findByProfileIdAndDateBetween(profileId, start, end);
        BigDecimal total = incomes.stream().map(IncomeEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<Long, List<IncomeEntity>> grouped = incomes.stream()
                .filter(i -> i.getCategory() != null)
                .collect(Collectors.groupingBy(i -> i.getCategory().getId()));

        List<CategoryBreakdownResponse.CategoryData> categories = grouped.entrySet().stream()
                .map(entry -> {
                    IncomeEntity sample = entry.getValue().get(0);
                    BigDecimal amount = entry.getValue().stream().map(IncomeEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal pct = total.compareTo(BigDecimal.ZERO) > 0
                            ? amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return CategoryBreakdownResponse.CategoryData.builder()
                            .categoryId(sample.getCategory().getId())
                            .categoryName(sample.getCategory().getName())
                            .categoryIcon(sample.getCategory().getIcon())
                            .amount(amount)
                            .percentage(pct)
                            .build();
                })
                .sorted(Comparator.comparing(CategoryBreakdownResponse.CategoryData::getAmount).reversed())
                .toList();

        return CategoryBreakdownResponse.builder()
                .month(month).type("income").total(total).categories(categories).build();
    }

    public List<IncomeDTO> getIncomeDataForRange(Long profileId, LocalDate start, LocalDate end) {
        return incomeRepository.findByProfileIdAndDateBetween(profileId, start, end)
                .stream().map(incomeMapper::toDTO).toList();
    }

    public List<ExpenseDTO> getExpenseDataForRange(Long profileId, LocalDate start, LocalDate end) {
        return expenseRepository.findByProfileIdAndDateBetween(profileId, start, end)
                .stream().map(expenseMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public BudgetSummaryResponse getBudgetSummary(String month) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        String budgetMonth = month != null ? month : java.time.YearMonth.now().toString();
        List<BudgetEntity> budgets = budgetRepository.findByProfileIdAndBudgetMonth(profile.getId(), budgetMonth);

        java.time.YearMonth ym = java.time.YearMonth.parse(budgetMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        List<ExpenseEntity> expenses = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), start, end);

        List<BudgetDTO> budgetDTOs = budgets.stream().map(b -> {
            BigDecimal spent = expenses.stream()
                    .filter(e -> e.getCategory() != null && e.getCategory().getId().equals(b.getCategory().getId()))
                    .map(ExpenseEntity::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return budgetMapper.toDTO(b, spent);
        }).toList();

        BigDecimal totalBudgeted = budgetDTOs.stream().map(BudgetDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSpent = budgetDTOs.stream().map(BudgetDTO::getSpent).reduce(BigDecimal.ZERO, BigDecimal::add);

        return BudgetSummaryResponse.builder()
                .budgetMonth(budgetMonth)
                .totalBudgeted(totalBudgeted)
                .totalSpent(totalSpent)
                .totalRemaining(totalBudgeted.subtract(totalSpent))
                .budgets(budgetDTOs)
                .build();
    }
}
