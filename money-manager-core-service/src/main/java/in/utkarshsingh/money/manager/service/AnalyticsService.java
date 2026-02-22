package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.response.CategoryBreakdownResponse;
import in.utkarshsingh.money.manager.dto.response.MonthlySummaryResponse;
import in.utkarshsingh.money.manager.dto.response.NetWorthResponse;
import in.utkarshsingh.money.manager.entity.ExpenseEntity;
import in.utkarshsingh.money.manager.entity.IncomeEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.repository.ExpenseRepository;
import in.utkarshsingh.money.manager.repository.IncomeRepository;
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

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final SavingsGoalService savingsGoalService;
    private final InvestmentService investmentService;
    private final DebtService debtService;
    private final ProfileService profileService;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    @Transactional(readOnly = true)
    public NetWorthResponse getNetWorth() {
        BigDecimal totalIncome = incomeService.getTotalIncomeForCurrentUser();
        BigDecimal totalExpense = expenseService.getTotalExpenseForCurrentUser();
        BigDecimal cashBalance = totalIncome.subtract(totalExpense);
        BigDecimal totalSavings = savingsGoalService.getTotalSaved();
        BigDecimal totalInvestments = investmentService.getTotalCurrentValue();
        BigDecimal totalDebt = debtService.getTotalDebt();
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
        ProfileEntity profile = profileService.getCurrentProfile();
        List<MonthlySummaryResponse.MonthData> months = new ArrayList<>();

        for (int m = 1; m <= 12; m++) {
            YearMonth ym = YearMonth.of(year, m);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();

            BigDecimal income = incomeRepository.findByProfileIdAndDateBetween(profile.getId(), start, end)
                    .stream().map(IncomeEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal expense = expenseRepository.findByProfileIdAndDateBetween(profile.getId(), start, end)
                    .stream().map(ExpenseEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

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
        ProfileEntity profile = profileService.getCurrentProfile();
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
}
