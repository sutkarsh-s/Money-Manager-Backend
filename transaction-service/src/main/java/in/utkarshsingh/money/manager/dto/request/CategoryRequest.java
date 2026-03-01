package in.utkarshsingh.money.manager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String icon;

    @NotBlank(message = "Type is required")
    @Pattern(regexp = "^(income|expense)$", message = "Type must be 'income' or 'expense'")
    private String type;
}
