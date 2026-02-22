package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.RecurringTransactionDTO;
import in.utkarshsingh.money.manager.dto.request.RecurringTransactionRequest;
import in.utkarshsingh.money.manager.service.RecurringTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/recurring-transactions")
public class RecurringTransactionController {

    private final RecurringTransactionService recurringTransactionService;

    @PostMapping
    public ResponseEntity<RecurringTransactionDTO> create(@Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recurringTransactionService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<RecurringTransactionDTO>> getAll() {
        return ResponseEntity.ok(recurringTransactionService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTransactionDTO> update(@PathVariable Long id,
                                                          @Valid @RequestBody RecurringTransactionRequest request) {
        return ResponseEntity.ok(recurringTransactionService.update(id, request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringTransactionDTO> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(recurringTransactionService.toggleActive(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        recurringTransactionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
