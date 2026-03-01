package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.LendBorrowDTO;
import in.utkarshsingh.money.manager.dto.LendBorrowPaymentDTO;
import in.utkarshsingh.money.manager.dto.PageResponseDTO;
import in.utkarshsingh.money.manager.dto.request.LendBorrowPaymentRequest;
import in.utkarshsingh.money.manager.dto.request.LendBorrowRequest;
import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import in.utkarshsingh.money.manager.enums.LendBorrowType;
import in.utkarshsingh.money.manager.service.LendBorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/lend-borrow")
public class LendBorrowController {

    private final LendBorrowService lendBorrowService;

    @PostMapping
    public ResponseEntity<LendBorrowDTO> create(@Valid @RequestBody LendBorrowRequest request) {
        LendBorrowDTO saved = lendBorrowService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<LendBorrowDTO>> getByType(
            @RequestParam LendBorrowType type,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "date") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder
    ) {
        PageResponseDTO<LendBorrowDTO> response = lendBorrowService.getByType(type, search, status, page, size, sortField, sortOrder);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LendBorrowDTO> update(@PathVariable Long id,
                                                @Valid @RequestBody LendBorrowRequest request) {
        LendBorrowDTO updated = lendBorrowService.updateEntry(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LendBorrowDTO> updateStatus(@PathVariable Long id,
                                                      @RequestParam LendBorrowStatus status) {
        LendBorrowDTO updated = lendBorrowService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/settlements")
    public ResponseEntity<LendBorrowPaymentDTO> addSettlement(@PathVariable Long id,
                                                              @Valid @RequestBody LendBorrowPaymentRequest request) {
        LendBorrowPaymentDTO payment = lendBorrowService.addSettlement(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @GetMapping("/{id}/settlements")
    public ResponseEntity<List<LendBorrowPaymentDTO>> getSettlements(@PathVariable Long id) {
        List<LendBorrowPaymentDTO> settlements = lendBorrowService.getSettlements(id);
        return ResponseEntity.ok(settlements);
    }

    @DeleteMapping("/{id}/settlements/{paymentId}")
    public ResponseEntity<Void> deleteSettlement(@PathVariable Long id,
                                                 @PathVariable Long paymentId) {
        lendBorrowService.deleteSettlement(id, paymentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lendBorrowService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
