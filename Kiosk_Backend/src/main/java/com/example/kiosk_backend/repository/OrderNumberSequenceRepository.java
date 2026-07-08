package com.example.kiosk_backend.repository;

import com.example.kiosk_backend.entity.OrderNumberSequence;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderNumberSequenceRepository extends JpaRepository<OrderNumberSequence, Integer> {

    /**
     * 싱글턴 카운터 행에 배타적 행 잠금(SELECT ... FOR UPDATE)을 걸어 조회한다.
     * 동시에 여러 주문이 생성되어도 이 행을 잠근 트랜잭션이 커밋될 때까지 다른 트랜잭션은 대기하므로
     * order_number 채번이 원자적으로 이루어진다(DB.md 6.3 참조).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM OrderNumberSequence s WHERE s.id = :id")
    Optional<OrderNumberSequence> findByIdForUpdate(@Param("id") Integer id);
}
