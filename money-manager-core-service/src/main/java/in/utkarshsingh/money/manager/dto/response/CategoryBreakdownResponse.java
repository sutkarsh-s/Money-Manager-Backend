package in.utkarshsingh.money.manager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryBreakdownResponse {

    private String month;
    private String type;
    private BigDecimal total;
    private List<CategoryData> categories;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CategoryData {
        private Long categoryId;
        private String categoryName;
        private String categoryIcon;
        private BigDecimal amount;
        private BigDecimal percentage;
    }
}
