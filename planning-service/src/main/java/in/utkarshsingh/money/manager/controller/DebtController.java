package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.DebtDTO;
import in.utkarshsingh.money.manager.dto.request.DebtRequest;
import in.utkarshsingh.money.manager.service.DebtService;
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
@RequestMapping("/v1/debts")
public class DebtController {

    private final DebtService debtService;

    @PostMapping
    public ResponseEntity<DebtDTO> create(@Valid @RequestBody DebtRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(debtService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<DebtDTO>> getAll() {
        return ResponseEntity.ok(debtService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DebtDTO> update(@PathVariable Long id, @Valid @RequestBody DebtRequest request) {
        return ResponseEntity.ok(debtService.update(id, request));
    }

    @PatchMapping("/{id}/payment")
    public ResponseEntity<DebtDTO> makePayment(@PathVariable Long id, @RequestBody Map<String, BigDecimal> body) {
        return ResponseEntity.ok(debtService.makePayment(id, body.get("amount")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        debtService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
