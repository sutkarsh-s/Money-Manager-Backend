package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.ContactMessageDTO;
import in.utkarshsingh.money.manager.dto.request.ContactRequest;
import in.utkarshsingh.money.manager.mapper.ContactMapper;
import in.utkarshsingh.money.manager.repository.ContactMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;
    private final ContactMapper contactMapper;

    @Transactional
    public ContactMessageDTO submit(ContactRequest request) {
        return contactMapper.toDTO(
                contactMessageRepository.save(contactMapper.toEntity(request))
        );
    }
}
