package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.LendBorrowDTO;
import in.utkarshsingh.money.manager.dto.PageResponseDTO;
import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import in.utkarshsingh.money.manager.enums.LendBorrowType;
import in.utkarshsingh.money.manager.service.LendBorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/lend-borrow")
public class LendBorrowController {

    private final LendBorrowService lendBorrowService;

    @PostMapping
    public ResponseEntity<LendBorrowDTO> create(@RequestBody LendBorrowDTO dto) {
        LendBorrowDTO saved = lendBorrowService.create(dto);
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

    @PatchMapping("/{id}/status")
    public ResponseEntity<LendBorrowDTO> updateStatus(@PathVariable Long id,
                                                      @RequestParam LendBorrowStatus status) {
        LendBorrowDTO updated = lendBorrowService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        lendBorrowService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
