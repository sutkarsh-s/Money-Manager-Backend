package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.InvestmentDTO;
import in.utkarshsingh.money.manager.dto.request.InvestmentRequest;
import in.utkarshsingh.money.manager.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<InvestmentDTO> create(@Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investmentService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<InvestmentDTO>> getAll() {
        return ResponseEntity.ok(investmentService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<InvestmentDTO> update(@PathVariable Long id,
                                                @Valid @RequestBody InvestmentRequest request) {
        return ResponseEntity.ok(investmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        investmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
