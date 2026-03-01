package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.response.BudgetSummaryResponse;
import in.utkarshsingh.money.manager.dto.response.CategoryBreakdownResponse;
import in.utkarshsingh.money.manager.dto.response.MonthlySummaryResponse;
import in.utkarshsingh.money.manager.dto.response.NetWorthResponse;
import in.utkarshsingh.money.manager.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Year;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/net-worth")
    public ResponseEntity<NetWorthResponse> getNetWorth() {
        return ResponseEntity.ok(analyticsService.getNetWorth());
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam(required = false) Integer year) {
        int effectiveYear = year != null ? year : Year.now().getValue();
        return ResponseEntity.ok(analyticsService.getMonthlySummary(effectiveYear));
    }

    @GetMapping("/category-breakdown")
    public ResponseEntity<CategoryBreakdownResponse> getCategoryBreakdown(
            @RequestParam(required = false) String month,
            @RequestParam(required = false, defaultValue = "expense") String type) {
        return ResponseEntity.ok(analyticsService.getCategoryBreakdown(month, type));
    }

    @GetMapping("/budget-summary")
    public ResponseEntity<BudgetSummaryResponse> getBudgetSummary(
            @RequestParam(required = false) String month) {
        return ResponseEntity.ok(analyticsService.getBudgetSummary(month));
    }
}
