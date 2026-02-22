package in.utkarshsingh.money.manager.mapper;

import in.utkarshsingh.money.manager.dto.CategoryDTO;
import in.utkarshsingh.money.manager.dto.request.CategoryRequest;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryEntity toEntity(CategoryRequest request, ProfileEntity profile) {
        return CategoryEntity.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .type(request.getType())
                .profile(profile)
                .build();
    }

    public CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ? entity.getProfile().getId() : null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .type(entity.getType())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
