package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    @Modifying
    void deleteAllByProfileId(Long profileId);

    List<CategoryEntity> findByProfileId(Long profileId);

    Optional<CategoryEntity> findByIdAndProfileId(Long id, Long profileId);

    List<CategoryEntity> findByTypeAndProfileId(String type, Long profileId);

    Boolean existsByNameAndProfileId(String name, Long profileId);
}
