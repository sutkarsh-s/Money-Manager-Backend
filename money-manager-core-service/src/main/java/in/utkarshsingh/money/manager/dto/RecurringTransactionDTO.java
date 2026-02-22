package in.utkarshsingh.money.manager.dto;

import in.utkarshsingh.money.manager.enums.TransactionFrequency;
import in.utkarshsingh.money.manager.enums.TransactionType;
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
public class RecurringTransactionDTO {

    private Long id;
    private String name;
    private String icon;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionFrequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private Boolean isActive;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
