package in.utkarshsingh.money.manager.service;

import in.utkarshsingh.money.manager.dto.CategoryDTO;
import in.utkarshsingh.money.manager.dto.request.CategoryRequest;
import in.utkarshsingh.money.manager.entity.CategoryEntity;
import in.utkarshsingh.money.manager.entity.ProfileEntity;
import in.utkarshsingh.money.manager.exceptions.ResourceNotFoundException;
import in.utkarshsingh.money.manager.mapper.CategoryMapper;
import in.utkarshsingh.money.manager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    public CategoryDTO saveCategory(CategoryRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        if (categoryRepository.existsByNameAndProfileId(request.getName().trim(), profile.getId())) {
            throw new IllegalArgumentException("Category with this name already exists");
        }
        CategoryEntity entity = categoryMapper.toEntity(request, profile);
        entity = categoryRepository.save(entity);
        return categoryMapper.toDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        return categoryRepository.findByProfileId(profile.getId())
                .stream().map(categoryMapper::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return categoryRepository.findByTypeAndProfileId(type, profile.getId())
                .stream().map(categoryMapper::toDTO).toList();
    }

    @Transactional
    public CategoryDTO updateCategory(Long categoryId, CategoryRequest request) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existing = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", categoryId));
        existing.setName(request.getName().trim());
        existing.setIcon(request.getIcon());
        existing = categoryRepository.save(existing);
        return categoryMapper.toDTO(existing);
    }
}
