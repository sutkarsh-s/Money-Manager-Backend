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
public class MonthlySummaryResponse {

    private int year;
    private List<MonthData> months;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MonthData {
        private int month;
        private String monthName;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal savings;
    }
}
