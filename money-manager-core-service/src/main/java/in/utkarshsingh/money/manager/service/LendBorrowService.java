package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.LendBorrowDTO;
import in.utkarshsingh.money.manager.dto.PageResponseDTO;
import in.utkarshsingh.money.manager.dto.request.LendBorrowRequest;
import in.utkarshsingh.money.manager.entity.LendBorrowEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import in.utkarshsingh.money.manager.enums.LendBorrowType;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.exceptions.UnauthorizedActionException;
import in.utkarshsingh.money.manager.mapper.LendBorrowMapper;
import in.utkarshsingh.money.manager.repository.LendBorrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LendBorrowService {

    private final LendBorrowRepository lendBorrowRepository;
    private final ProfileService profileService;
    private final LendBorrowMapper lendBorrowMapper;

    @Transactional
    public LendBorrowDTO create(LendBorrowRequest request) {
        if (request.getDueDate().isBefore(request.getDate())) {
            throw new IllegalArgumentException("Due date cannot be before transaction date");
        }
        ProfileEntity profile = profileService.getCurrentProfile();
        LendBorrowEntity saved = lendBorrowRepository.save(lendBorrowMapper.toEntity(request, profile));
        return lendBorrowMapper.toDTO(saved);
    }

    @Transactional(readOnly = true)
    public PageResponseDTO<LendBorrowDTO> getByType(LendBorrowType type,
                                                    String search,
                                                    String status,
                                                    int page,
                                                    int size,
                                                    String sortField,
                                                    String sortOrder) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), 100),
                Sort.by(direction, sanitizeSortField(sortField))
        );
        String normalizedSearch = search == null ? "" : search.trim();

        Page<LendBorrowEntity> response;
        if ("OVERDUE".equalsIgnoreCase(status)) {
            response = lendBorrowRepository.findOverdueByFilters(profile.getId(), type, normalizedSearch, LocalDate.now(), pageable);
        } else {
            LendBorrowStatus parsedStatus = parseStatus(status);
            response = lendBorrowRepository.findAllByFilters(profile.getId(), type, normalizedSearch, parsedStatus, pageable);
        }

        return PageResponseDTO.<LendBorrowDTO>builder()
                .content(response.getContent().stream().map(lendBorrowMapper::toDTO).toList())
                .page(response.getNumber())
                .size(response.getSize())
                .totalElements(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .first(response.isFirst())
                .last(response.isLast())
                .build();
    }

    @Transactional
    public LendBorrowDTO updateStatus(Long id, LendBorrowStatus status) {
        if (status == null || status == LendBorrowStatus.OVERDUE) {
            throw new IllegalArgumentException("Status must be PENDING or PAID");
        }
        ProfileEntity profile = profileService.getCurrentProfile();
        LendBorrowEntity entity = lendBorrowRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Lend/Borrow entry", id));
        entity.setStatus(status);
        LendBorrowEntity updated = lendBorrowRepository.save(entity);
        return lendBorrowMapper.toDTO(updated);
    }

    @Transactional
    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        LendBorrowEntity entity = lendBorrowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lend/Borrow entry", id));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedActionException("You are not allowed to delete this entry");
        }
        lendBorrowRepository.delete(entity);
    }

    private String sanitizeSortField(String sortField) {
        if ("amount".equalsIgnoreCase(sortField) || "dueDate".equalsIgnoreCase(sortField) || "name".equalsIgnoreCase(sortField)) {
            return sortField;
        }
        return "date";
    }

    private LendBorrowStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            LendBorrowStatus parsed = LendBorrowStatus.valueOf(status.trim().toUpperCase());
            return parsed == LendBorrowStatus.OVERDUE ? null : parsed;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status filter. Allowed values: PENDING, PAID, OVERDUE");
        }
    }
}
