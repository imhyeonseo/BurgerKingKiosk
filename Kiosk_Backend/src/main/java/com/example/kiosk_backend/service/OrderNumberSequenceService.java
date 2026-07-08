package com.example.kiosk_backend.service;

import com.example.kiosk_backend.entity.OrderNumberSequence;
import com.example.kiosk_backend.repository.OrderNumberSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** DB.md 6.3의 원자적 채번 절차를 캡슐화한다. 반드시 호출자의 트랜잭션 안에서 실행되어야 한다. */
@Service
@RequiredArgsConstructor
public class OrderNumberSequenceService {

    private static final Integer SEQUENCE_ID = 1;

    private final OrderNumberSequenceRepository orderNumberSequenceRepository;

    public int issueOrderNumber() {
        OrderNumberSequence sequence = orderNumberSequenceRepository.findByIdForUpdate(SEQUENCE_ID)
                .orElseThrow(() -> new IllegalStateException("order_number_sequence 초기 데이터가 존재하지 않습니다."));
        return sequence.issueAndIncrement();
    }
}
