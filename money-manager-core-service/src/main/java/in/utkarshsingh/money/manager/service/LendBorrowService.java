package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.LendBorrowDTO;
import in.utkarshsingh.money.manager.dto.PageResponseDTO;
import in.utkarshsingh.money.manager.entity.LendBorrowEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import in.utkarshsingh.money.manager.enums.LendBorrowType;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.exceptions.UnauthorizedActionException;
import in.utkarshsingh.money.manager.repository.LendBorrowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LendBorrowService {

    private final LendBorrowRepository lendBorrowRepository;
    private final ProfileService profileService;

    public LendBorrowDTO create(LendBorrowDTO dto) {
        validateRequest(dto);
        ProfileEntity profile = profileService.getCurrentProfile();
        LendBorrowEntity saved = lendBorrowRepository.save(toEntity(dto, profile));
        return toDTO(saved);
    }

    public PageResponseDTO<LendBorrowDTO> getByType(LendBorrowType type,
                                                    String search,
                                                    String status,
                                                    int page,
                                                    int size,
                                                    String sortField,
                                                    String sortOrder) {
        ProfileEntity profile = profileService.getCurrentProfile();
        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by(direction, sanitizeSortField(sortField)));
        String normalizedSearch = search == null ? "" : search.trim();

        Page<LendBorrowEntity> response;
        if ("OVERDUE".equalsIgnoreCase(status)) {
            response = lendBorrowRepository.findOverdueByFilters(profile.getId(), type, normalizedSearch, LocalDate.now(), pageable);
        } else {
            LendBorrowStatus parsedStatus = parseStatus(status);
            response = lendBorrowRepository.findAllByFilters(profile.getId(), type, normalizedSearch, parsedStatus, pageable);
        }

        return PageResponseDTO.<LendBorrowDTO>builder()
                .content(response.getContent().stream().map(this::toDTO).toList())
                .page(response.getNumber())
                .size(response.getSize())
                .totalElements(response.getTotalElements())
                .totalPages(response.getTotalPages())
                .first(response.isFirst())
                .last(response.isLast())
                .build();
    }

    public LendBorrowDTO updateStatus(Long id, LendBorrowStatus status) {
        if (status == null || status == LendBorrowStatus.OVERDUE) {
            throw new IllegalArgumentException("Status must be PENDING or PAID");
        }
        ProfileEntity profile = profileService.getCurrentProfile();
        LendBorrowEntity entity = lendBorrowRepository.findByIdAndProfileId(id, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Lend/Borrow entry", id));
        entity.setStatus(status);
        LendBorrowEntity updated = lendBorrowRepository.save(entity);
        return toDTO(updated);
    }

    public void delete(Long id) {
        ProfileEntity profile = profileService.getCurrentProfile();
        LendBorrowEntity entity = lendBorrowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lend/Borrow entry", id));
        if (!entity.getProfile().getId().equals(profile.getId())) {
            throw new UnauthorizedActionException("You are not allowed to delete this entry");
        }
        lendBorrowRepository.delete(entity);
    }

    private void validateRequest(LendBorrowDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.getType() == null) {
            throw new IllegalArgumentException("Type is required");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (dto.getPersonName() == null || dto.getPersonName().trim().isEmpty()) {
            throw new IllegalArgumentException("Person name is required");
        }
        if (dto.getAmount() == null || dto.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (dto.getDate() == null) {
            throw new IllegalArgumentException("Transaction date is required");
        }
        if (dto.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        if (dto.getDueDate().isBefore(dto.getDate())) {
            throw new IllegalArgumentException("Due date cannot be before transaction date");
        }
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

    private LendBorrowEntity toEntity(LendBorrowDTO dto, ProfileEntity profile) {
        LendBorrowStatus status = dto.getStatus() == null || dto.getStatus() == LendBorrowStatus.OVERDUE
                ? LendBorrowStatus.PENDING
                : dto.getStatus();
        return LendBorrowEntity.builder()
                .name(dto.getName().trim())
                .icon(dto.getIcon())
                .personName(dto.getPersonName().trim())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .dueDate(dto.getDueDate())
                .notes(dto.getNotes())
                .type(dto.getType())
                .status(status)
                .profile(profile)
                .build();
    }

    private LendBorrowDTO toDTO(LendBorrowEntity entity) {
        LendBorrowStatus effectiveStatus = entity.getStatus();
        if (effectiveStatus == LendBorrowStatus.PENDING && entity.getDueDate() != null && entity.getDueDate().isBefore(LocalDate.now())) {
            effectiveStatus = LendBorrowStatus.OVERDUE;
        }
        return LendBorrowDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .personName(entity.getPersonName())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .dueDate(entity.getDueDate())
                .notes(entity.getNotes())
                .type(entity.getType())
                .status(effectiveStatus)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
