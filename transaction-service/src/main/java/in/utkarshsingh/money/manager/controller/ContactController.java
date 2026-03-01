package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.ContactMessageDTO;
import in.utkarshsingh.money.manager.dto.request.ContactRequest;
import in.utkarshsingh.money.manager.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/support/contact")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactMessageDTO> submit(@Valid @RequestBody ContactRequest request) {
        ContactMessageDTO saved = contactService.submit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
