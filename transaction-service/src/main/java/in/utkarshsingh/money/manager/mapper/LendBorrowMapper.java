package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.LendBorrowDTO;
import in.utkarshsingh.money.manager.dto.LendBorrowPaymentDTO;
import in.utkarshsingh.money.manager.dto.request.LendBorrowRequest;
import in.utkarshsingh.money.manager.entity.LendBorrowEntity;
import in.utkarshsingh.money.manager.entity.LendBorrowPaymentEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
        return toDTO(entity, BigDecimal.ZERO, null);
    }

    public LendBorrowDTO toDTO(LendBorrowEntity entity, BigDecimal paidAmount, List<LendBorrowPaymentEntity> settlements) {
        LendBorrowStatus effectiveStatus = entity.getStatus();
        if (effectiveStatus == LendBorrowStatus.PENDING
                && entity.getDueDate() != null
                && entity.getDueDate().isBefore(LocalDate.now())) {
            effectiveStatus = LendBorrowStatus.OVERDUE;
        }

        List<LendBorrowPaymentDTO> paymentDTOs = settlements != null
                ? settlements.stream().map(this::toPaymentDTO).toList()
                : null;

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
                .paidAmount(paidAmount != null ? paidAmount : BigDecimal.ZERO)
                .settlements(paymentDTOs)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public LendBorrowPaymentDTO toPaymentDTO(LendBorrowPaymentEntity entity) {
        return LendBorrowPaymentDTO.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .date(entity.getDate())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
