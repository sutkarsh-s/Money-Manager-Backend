package in.utkarshsingh.money.manager.dto.request;

import in.utkarshsingh.money.manager.enums.LendBorrowType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LendBorrowRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String icon;

    @NotBlank(message = "Person name is required")
    private String personName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    @NotNull(message = "Transaction date is required")
    private LocalDate date;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    private String notes;

    @NotNull(message = "Type is required")
    private LendBorrowType type;
}
