package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.ContactMessageDTO;
import in.utkarshsingh.money.manager.entity.ContactMessageEntity;
import in.utkarshsingh.money.manager.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageDTO submit(ContactMessageDTO dto) {
        validate(dto);
        ContactMessageEntity saved = contactMessageRepository.save(ContactMessageEntity.builder()
                .name(dto.getName().trim())
                .email(dto.getEmail().trim())
                .subject(dto.getSubject() == null ? "General inquiry" : dto.getSubject().trim())
                .message(dto.getMessage().trim())
                .build());

        return ContactMessageDTO.builder()
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .subject(saved.getSubject())
                .message(saved.getMessage())
                .status("SUBMITTED")
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private void validate(ContactMessageDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (dto.getEmail() == null || !EMAIL_PATTERN.matcher(dto.getEmail().trim()).matches()) {
            throw new IllegalArgumentException("A valid email is required");
        }
        if (dto.getMessage() == null || dto.getMessage().trim().length() < 10) {
            throw new IllegalArgumentException("Message must be at least 10 characters");
        }
    }
}
