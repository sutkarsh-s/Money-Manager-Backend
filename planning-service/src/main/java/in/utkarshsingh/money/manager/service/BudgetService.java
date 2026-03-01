package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.BudgetDTO;
import in.utkarshsingh.money.manager.dto.request.BudgetRequest;
import in.utkarshsingh.money.manager.dto.response.BudgetSummaryResponse;
import in.utkarshsingh.money.manager.entity.BudgetEntity;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.mapper.BudgetMapper;
import in.utkarshsingh.money.manager.repository.BudgetRepository;
import in.utkarshsingh.money.manager.repository.CategoryRepository;
import in.utkarshsingh.money.manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserResolverService userResolverService;
    private final BudgetMapper budgetMapper;

    @Transactional
    public BudgetDTO createBudget(BudgetRequest request) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        if (budgetRepository.existsByProfileIdAndCategoryIdAndBudgetMonth(
                profile.getId(), request.getCategoryId(), request.getBudgetMonth())) {
            throw new IllegalArgumentException("Budget already exists for this category and month");
        }

        BudgetEntity entity = budgetMapper.toEntity(request, profile, category);
        entity = budgetRepository.save(entity);
        BigDecimal spent = getSpentForBudget(profile.getId(), category.getId(), request.getBudgetMonth());
        return budgetMapper.toDTO(entity, spent);
    }

    @Transactional(readOnly = true)
    public BudgetSummaryResponse getBudgetSummary(String month) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        String budgetMonth = month != null ? month : YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        List<BudgetEntity> budgets = budgetRepository.findByProfileIdAndBudgetMonth(profile.getId(), budgetMonth);

        List<BudgetDTO> budgetDTOs = budgets.stream().map(b -> {
            BigDecimal spent = getSpentForBudget(profile.getId(), b.getCategory().getId(), budgetMonth);
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

    @Transactional
    public BudgetDTO updateBudget(Long id, BudgetRequest request) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        BudgetEntity entity = budgetRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        entity.setAmount(request.getAmount());
        entity = budgetRepository.save(entity);
        BigDecimal spent = getSpentForBudget(profile.getId(), entity.getCategory().getId(), entity.getBudgetMonth());
        return budgetMapper.toDTO(entity, spent);
    }

    @Transactional
    public void deleteBudget(Long id) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        BudgetEntity entity = budgetRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Budget", id));
        budgetRepository.delete(entity);
    }

    private BigDecimal getSpentForBudget(Long profileId, Long categoryId, String budgetMonth) {
        YearMonth ym = YearMonth.parse(budgetMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        return expenseRepository.findByProfileIdAndDateBetween(profileId, start, end).stream()
                .filter(e -> e.getCategory() != null && e.getCategory().getId().equals(categoryId))
                .map(e -> e.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
