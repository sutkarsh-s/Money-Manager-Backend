package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.ExpenseDTO;
import in.utkarshsingh.money.manager.dto.request.ExpenseRequest;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.ExpenseEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.exceptions.UnauthorizedActionException;
import in.utkarshsingh.money.manager.mapper.ExpenseMapper;
import in.utkarshsingh.money.manager.repository.CategoryRepository;
import in.utkarshsingh.money.manager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final UserResolverService userResolverService;
    private final ExpenseMapper expenseMapper;

    @Transactional
    public ExpenseDTO addExpense(ExpenseRequest request) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        ExpenseEntity entity = expenseMapper.toEntity(request, profile, category);
        entity = expenseRepository.save(entity);
        return expenseMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        return expenseRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate)
                .stream().map(expenseMapper::toDTO).toList();
    }

    @Transactional
    public ExpenseDTO updateExpense(Long id, ExpenseRequest request) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Expense", id));
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        entity.setName(request.getName().trim());
        entity.setIcon(request.getIcon());
        entity.setAmount(request.getAmount());
        entity.setDate(request.getDate());
        entity.setCategory(category);
        entity = expenseRepository.save(entity);
        return expenseMapper.toDTO(entity);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedActionException("You are not allowed to delete this expense");
        }
        expenseRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser() {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        return expenseRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId())
                .stream().map(expenseMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalExpenseForCurrentUser() {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = userResolverService.getCurrentProfile();
        return expenseRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(), startDate, endDate, keyword, sort)
                .stream().map(expenseMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date) {
        return expenseRepository.findByProfileIdAndDate(profileId, date)
                .stream().map(expenseMapper::toDTO).toList();
    }
}
