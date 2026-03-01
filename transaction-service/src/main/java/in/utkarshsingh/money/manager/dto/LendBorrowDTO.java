package in.utkarshsingh.money.manager.dto;

import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import in.utkarshsingh.money.manager.enums.LendBorrowType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LendBorrowDTO {
    private Long id;
    private String name;
    private String icon;
    private String personName;
    private BigDecimal amount;
    private LocalDate date;
    private LocalDate dueDate;
    private String notes;
    private LendBorrowType type;
    private LendBorrowStatus status;
    private BigDecimal paidAmount;
    private List<LendBorrowPaymentDTO> settlements;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
