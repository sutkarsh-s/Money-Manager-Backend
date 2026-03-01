package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.SavingsGoalDTO;
import in.utkarshsingh.money.manager.dto.request.SavingsGoalRequest;
import in.utkarshsingh.money.manager.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @PostMapping
    public ResponseEntity<SavingsGoalDTO> create(@Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(savingsGoalService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<SavingsGoalDTO>> getAll() {
        return ResponseEntity.ok(savingsGoalService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalDTO> update(@PathVariable Long id,
                                                 @Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.ok(savingsGoalService.update(id, request));
    }

    @PatchMapping("/{id}/contribute")
    public ResponseEntity<SavingsGoalDTO> contribute(@PathVariable Long id,
                                                     @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(savingsGoalService.contribute(id, body.get("amount")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        savingsGoalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
