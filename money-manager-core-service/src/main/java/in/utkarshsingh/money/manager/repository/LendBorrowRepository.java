package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.LendBorrowEntity;
import in.utkarshsingh.money.manager.enums.LendBorrowStatus;
import in.utkarshsingh.money.manager.enums.LendBorrowType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface LendBorrowRepository extends JpaRepository<LendBorrowEntity, Long> {

    @Query("""
            SELECT lb FROM LendBorrowEntity lb
            WHERE lb.profile.id = :profileId
            AND lb.type = :type
            AND (
                :search = ''
                OR LOWER(lb.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(lb.personName) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (:status IS NULL OR lb.status = :status)
            """)
    Page<LendBorrowEntity> findAllByFilters(@Param("profileId") Long profileId,
                                            @Param("type") LendBorrowType type,
                                            @Param("search") String search,
                                            @Param("status") LendBorrowStatus status,
                                            Pageable pageable);

    @Query("""
            SELECT lb FROM LendBorrowEntity lb
            WHERE lb.profile.id = :profileId
            AND lb.type = :type
            AND lb.status = in.utkarshsingh.money.manager.enums.LendBorrowStatus.PENDING
            AND lb.dueDate < :today
            AND (
                :search = ''
                OR LOWER(lb.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(lb.personName) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<LendBorrowEntity> findOverdueByFilters(@Param("profileId") Long profileId,
                                                @Param("type") LendBorrowType type,
                                                @Param("search") String search,
                                                @Param("today") LocalDate today,
                                                Pageable pageable);

    Optional<LendBorrowEntity> findByIdAndProfileId(Long id, Long profileId);
}
