package in.utkarshsingh.money.manager.repository;

import in.utkarshsingh.money.manager.entity.LendBorrowPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface LendBorrowPaymentRepository extends JpaRepository<LendBorrowPaymentEntity, Long> {

    List<LendBorrowPaymentEntity> findByLendBorrowIdOrderByDateDesc(Long lendBorrowId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM LendBorrowPaymentEntity p WHERE p.lendBorrow.id = :lendBorrowId")
    BigDecimal sumByLendBorrowId(@Param("lendBorrowId") Long lendBorrowId);

    void deleteAllByLendBorrowId(Long lendBorrowId);
}
