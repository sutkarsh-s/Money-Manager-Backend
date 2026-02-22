package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.LendBorrowDTO;
import in.utkarshsingh.money.manager.dto.request.LendBorrowRequest;
import in.utkarshsingh.money.manager.entity.LendBorrowEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class LendBorrowMapper {

    public LendBorrowEntity toEntity(LendBorrowRequest request, ProfileEntity profile) {
        return LendBorrowEntity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .personName(request.getPersonName().trim())
                .amount(request.getAmount())
                .date(request.getDate())
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .type(request.getType())
                .status(LendBorrowStatus.PENDING)
                .profile(profile)
                .build();
    }

    public LendBorrowDTO toDTO(LendBorrowEntity entity) {
        LendBorrowStatus effectiveStatus = entity.getStatus();
        if (effectiveStatus == LendBorrowStatus.PENDING
                && entity.getDueDate() != null
                && entity.getDueDate().isBefore(LocalDate.now())) {
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
