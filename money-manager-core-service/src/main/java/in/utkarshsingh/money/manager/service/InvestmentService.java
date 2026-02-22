package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.InvestmentDTO;
import in.utkarshsingh.money.manager.dto.request.InvestmentRequest;
import in.utkarshsingh.money.manager.entity.InvestmentEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.exceptions.UnauthorizedActionException;
import in.utkarshsingh.money.manager.mapper.InvestmentMapper;
import in.utkarshsingh.money.manager.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;
    private final ProfileService profileService;
    private final InvestmentMapper mapper;

    @Transactional
    public InvestmentDTO create(InvestmentRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        InvestmentEntity entity = mapper.toEntity(request, profile);
        entity = investmentRepository.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<InvestmentDTO> getAll() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return investmentRepository.findByProfileIdOrderByCreatedAtDesc(profile.getId())
                .stream().map(mapper::toDTO).toList();
    }

    @Transactional
    public InvestmentDTO update(Long id, InvestmentRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        InvestmentEntity entity = investmentRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Investment", id));
        entity.setName(request.getName().trim());
        entity.setIcon(request.getIcon());
        entity.setType(request.getType());
        entity.setInvestedAmount(request.getInvestedAmount());
        entity.setCurrentValue(request.getCurrentValue());
        entity.setPurchaseDate(request.getPurchaseDate());
        entity.setNotes(request.getNotes());
        entity = investmentRepository.save(entity);
        return mapper.toDTO(entity);
    }

    @Transactional
    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        InvestmentEntity entity = investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment", id));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedActionException("You are not allowed to delete this investment");
        }
        investmentRepository.delete(entity);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalInvested() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return investmentRepository.findTotalInvestedByProfileId(profile.getId());
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalCurrentValue() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return investmentRepository.findTotalCurrentValueByProfileId(profile.getId());
    }
}
