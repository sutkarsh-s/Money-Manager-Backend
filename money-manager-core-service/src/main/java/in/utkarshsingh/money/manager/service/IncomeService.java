package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.IncomeDTO;
import in.utkarshsingh.money.manager.dto.request.IncomeRequest;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.IncomeEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.exceptions.UnauthorizedActionException;
import in.utkarshsingh.money.manager.mapper.IncomeMapper;
import in.utkarshsingh.money.manager.repository.CategoryRepository;
import in.utkarshsingh.money.manager.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeService {

    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;
    private final IncomeMapper incomeMapper;

    @Transactional
    public IncomeDTO addIncome(IncomeRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        IncomeEntity entity = incomeMapper.toEntity(request, profile, category);
        entity = incomeRepository.save(entity);
        return incomeMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        return incomeRepository.findByProfileIdAndDateBetween(profile.getId(), startDate, endDate)
                .stream().map(incomeMapper::toDTO).toList();
    }

    @Transactional
    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepository.findById(incomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Income", incomeId));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedActionException("You are not allowed to delete this income");
        }
        incomeRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public List<IncomeDTO> getLatest5IncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return incomeRepository.findTop5ByProfileIdOrderByDateDesc(profile.getId())
                .stream().map(incomeMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return incomeRepository.findByProfileIdAndDateBetweenAndNameContainingIgnoreCase(
                        profile.getId(), startDate, endDate, keyword, sort)
                .stream().map(incomeMapper::toDTO).toList();
    }
}
