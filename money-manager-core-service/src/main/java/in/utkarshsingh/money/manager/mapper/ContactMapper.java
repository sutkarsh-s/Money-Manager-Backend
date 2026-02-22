package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.ContactMessageDTO;
import in.utkarshsingh.money.manager.dto.request.ContactRequest;
import in.utkarshsingh.money.manager.entity.ContactMessageEntity;
import org.springframework.stereotype.Component;

@Component
public class ContactMapper {

    public ContactMessageEntity toEntity(ContactRequest request) {
        return ContactMessageEntity.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim())
                .subject(request.getSubject() == null ? "General inquiry" : request.getSubject().trim())
                .message(request.getMessage().trim())
                .build();
    }

    public ContactMessageDTO toDTO(ContactMessageEntity entity) {
        return ContactMessageDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .subject(entity.getSubject())
                .message(entity.getMessage())
                .status("SUBMITTED")
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
