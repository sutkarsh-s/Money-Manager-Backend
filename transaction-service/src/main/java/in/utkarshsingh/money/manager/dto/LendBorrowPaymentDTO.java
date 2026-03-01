package in.utkarshsingh.money.manager.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LendBorrowPaymentDTO {
    private Long id;
    private BigDecimal amount;
    private LocalDate date;
    private String notes;
    private LocalDateTime createdAt;
}
