package in.utkarshsingh.money.manager.dto.request;

import in.utkarshsingh.money.manager.enums.InvestmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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
public class InvestmentRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String icon;

    @NotNull(message = "Investment type is required")
    private InvestmentType type;

    @NotNull(message = "Invested amount is required")
    @DecimalMin(value = "0.01", message = "Invested amount must be greater than zero")
    private BigDecimal investedAmount;

    @NotNull(message = "Current value is required")
    @DecimalMin(value = "0.00", message = "Current value cannot be negative")
    private BigDecimal currentValue;

    @NotNull(message = "Purchase date is required")
    @PastOrPresent(message = "Purchase date cannot be in the future")
    private LocalDate purchaseDate;

    private String notes;
}
