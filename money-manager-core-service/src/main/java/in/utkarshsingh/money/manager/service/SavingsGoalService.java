package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.SavingsGoalDTO;
import in.utkarshsingh.money.manager.dto.request.SavingsGoalRequest;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.entity.SavingsGoalEntity;
import in.utkarshsingh.money.manager.enums.GoalStatus;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.exceptions.UnauthorizedActionException;
import in.utkarshsingh.money.manager.mapper.SavingsGoalMapper;
import in.utkarshsingh.money.manager.repository.SavingsGoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final ProfileService profileService;
    private final SavingsGoalMapper mapper;

    @Transactional
    public SavingsGoalDTO create(SavingsGoalRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SavingsGoalEntity entity = mapper.toEntity(request, profile);
        entity = savingsGoalRepository.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<SavingsGoalDTO> getAll() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return savingsGoalRepository.findByProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public SavingsGoalDTO update(Long id, SavingsGoalRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SavingsGoalEntity entity = savingsGoalRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal", id));
        entity.setName(request.getName().trim());
        entity.setIcon(request.getIcon());
        entity.setTargetAmount(request.getTargetAmount());
        entity.setTargetDate(request.getTargetDate());
        entity = savingsGoalRepository.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public SavingsGoalDTO contribute(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Contribution amount must be greater than zero");
        }
        ProfileEntity profile = profileService.getCurrentProfile();
        SavingsGoalEntity entity = savingsGoalRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal", id));

        if (entity.getStatus() != GoalStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot contribute to a " + entity.getStatus().name().toLowerCase() + " goal");
        }

        entity.setCurrentAmount(entity.getCurrentAmount().add(amount));
        if (entity.getCurrentAmount().compareTo(entity.getTargetAmount()) >= 0) {
            entity.setStatus(GoalStatus.COMPLETED);
        }
        entity = savingsGoalRepository.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        SavingsGoalEntity entity = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal", id));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedActionException("You are not allowed to delete this savings goal");
        }
        savingsGoalRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalSaved() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return savingsGoalRepository.findTotalSavedByProfileId(profile.getId());
    }
}
