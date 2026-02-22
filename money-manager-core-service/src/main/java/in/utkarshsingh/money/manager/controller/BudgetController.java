package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.BudgetDTO;
import in.utkarshsingh.money.manager.dto.request.BudgetRequest;
import in.utkarshsingh.money.manager.dto.response.BudgetSummaryResponse;
import in.utkarshsingh.money.manager.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetDTO> create(@Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(request));
    }

    @GetMapping("/summary")
    public ResponseEntity<BudgetSummaryResponse> getSummary(@RequestParam(required = false) String month) {
        return ResponseEntity.ok(budgetService.getBudgetSummary(month));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDTO> update(@PathVariable Long id, @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        budgetService.deleteBudget(id);
        return ResponseEntity.noContent().build();
    }
}
