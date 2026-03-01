package in.utkarshsingh.money.manager.dto.request;

import in.utkarshsingh.money.manager.enums.DebtType;
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
public class DebtRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String icon;

    @NotNull(message = "Debt type is required")
    private DebtType type;

    @NotNull(message = "Original amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal originalAmount;

    private BigDecimal interestRate;

    private BigDecimal emiAmount;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private String notes;
}
