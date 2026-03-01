package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.ProfileDTO;
import in.utkarshsingh.money.manager.dto.UserPublicDTO;
import in.utkarshsingh.money.manager.dto.request.RegisterRequest;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public ProfileEntity toEntity(RegisterRequest request) {
        return ProfileEntity.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .profileImageUrl(request.getProfileImageUrl())
                .build();
    }

    public ProfileDTO toDTO(ProfileEntity entity) {
        return ProfileDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .profileImageUrl(entity.getProfileImageUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public UserPublicDTO toPublicDTO(ProfileEntity entity) {
        return UserPublicDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .profileImageUrl(entity.getProfileImageUrl())
                .build();
    }
}
