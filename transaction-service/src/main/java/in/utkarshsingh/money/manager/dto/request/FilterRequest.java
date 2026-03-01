package in.utkarshsingh.money.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterRequest {

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(income|expense)$", message = "Type must be 'income' or 'expense'")
    private String type;

    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private String sortField;
    private String sortOrder;
}
