package in.utkarshsingh.money.manager.dto;

import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import in.utkarshsingh.money.manager.enums.LendBorrowType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
