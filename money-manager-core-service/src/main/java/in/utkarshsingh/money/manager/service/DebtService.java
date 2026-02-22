package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.DebtDTO;
import in.utkarshsingh.money.manager.dto.request.DebtRequest;
import in.utkarshsingh.money.manager.entity.DebtEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.enums.DebtStatus;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.exceptions.UnauthorizedActionException;
import in.utkarshsingh.money.manager.mapper.DebtMapper;
import in.utkarshsingh.money.manager.repository.DebtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final ProfileService profileService;
    private final DebtMapper mapper;

    @Transactional
    public DebtDTO create(DebtRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        DebtEntity entity = mapper.toEntity(request, profile);
        entity = debtRepository.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<DebtDTO> getAll() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return debtRepository.findByProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public DebtDTO update(Long id, DebtRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        DebtEntity entity = debtRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Debt", id));
        entity.setName(request.getName().trim());
        entity.setIcon(request.getIcon());
        entity.setType(request.getType());
        entity.setInterestRate(request.getInterestRate());
        entity.setEmiAmount(request.getEmiAmount());
        entity.setEndDate(request.getEndDate());
        entity.setNotes(request.getNotes());
        entity = debtRepository.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public DebtDTO makePayment(Long id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        ProfileEntity profile = profileService.getCurrentProfile();
        DebtEntity entity = debtRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Debt", id));

        if (entity.getStatus() == DebtStatus.PAID_OFF) {
            throw new IllegalArgumentException("This debt is already paid off");
        }

        BigDecimal newRemaining = entity.getRemainingAmount().subtract(amount);
        if (newRemaining.compareTo(BigDecimal.ZERO) <= 0) {
            entity.setRemainingAmount(BigDecimal.ZERO);
            entity.setStatus(DebtStatus.PAID_OFF);
        } else {
            entity.setRemainingAmount(newRemaining);
        }
        entity = debtRepository.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        DebtEntity entity = debtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Debt", id));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedActionException("You are not allowed to delete this debt");
        }
        debtRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalDebt() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return debtRepository.findTotalRemainingDebtByProfileId(profile.getId());
    }
}
