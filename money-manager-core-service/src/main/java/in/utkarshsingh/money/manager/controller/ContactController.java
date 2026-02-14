package in.utkarshsingh.money.manager.controller;

import in.utkarshsingh.money.manager.dto.ContactMessageDTO;
import in.utkarshsingh.money.manager.service.ContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactMessageDTO> submit(@RequestBody ContactMessageDTO dto) {
        ContactMessageDTO saved = contactService.submit(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
