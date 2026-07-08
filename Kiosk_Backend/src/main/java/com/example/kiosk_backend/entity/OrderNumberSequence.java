package com.example.kiosk_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * orders.order_number 채번용 싱글턴 카운터 테이블. id는 항상 1이다.
 * 실제 원자적 증가는 OrderNumberSequenceRepository의 UPDATE 쿼리로 수행하며,
 * 이 엔티티는 초기 데이터 적재 및 조회 용도로만 사용한다.
 */
@Getter
@Entity
@Table(name = "order_number_sequence")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderNumberSequence {

    @Id
    private Integer id;

    @Column(name = "next_value", nullable = false)
    private Integer nextValue;

    public OrderNumberSequence(Integer id, Integer nextValue) {
        this.id = id;
        this.nextValue = nextValue;
    }

    /** 현재 값을 발급하고 다음 값을 1 증가시킨다. */
    public int issueAndIncrement() {
        int issued = this.nextValue;
        this.nextValue = issued + 1;
        return issued;
    }
}
