package com.example.kiosk_backend.config;

import com.example.kiosk_backend.entity.Admin;
import com.example.kiosk_backend.entity.Category;
import com.example.kiosk_backend.entity.Menu;
import com.example.kiosk_backend.entity.OrderNumberSequence;
import com.example.kiosk_backend.entity.SetMenuItem;
import com.example.kiosk_backend.repository.AdminRepository;
import com.example.kiosk_backend.repository.CategoryRepository;
import com.example.kiosk_backend.repository.MenuRepository;
import com.example.kiosk_backend.repository.OrderNumberSequenceRepository;
import com.example.kiosk_backend.repository.SetMenuItemRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 최초 구동 시 필요한 초기 데이터를 적재한다.
 * order_number_sequence는 애플리케이션 동작에 필수적이므로 반드시 존재해야 한다(DB.md 6.3 참조).
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional
public class DataInitializer implements ApplicationRunner {

    private static final Integer SEQUENCE_ID = 1;
    private static final int SEQUENCE_START = 101;

    private final OrderNumberSequenceRepository orderNumberSequenceRepository;
    private final AdminRepository adminRepository;
    private final CategoryRepository categoryRepository;
    private final MenuRepository menuRepository;
    private final SetMenuItemRepository setMenuItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        initOrderNumberSequence();
        initAdmin();
        initCategoriesAndMenus();
    }

    private void initOrderNumberSequence() {
        if (orderNumberSequenceRepository.findById(SEQUENCE_ID).isEmpty()) {
            orderNumberSequenceRepository.save(new OrderNumberSequence(SEQUENCE_ID, SEQUENCE_START));
            log.info("order_number_sequence 초기화 완료 (시작 번호: {})", SEQUENCE_START);
        }
    }

    private void initAdmin() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin("admin", passwordEncoder.encode("password123"), "관리자");
            adminRepository.save(admin);
            log.info("기본 관리자 계정 생성 완료 (username=admin, password=password123 — 운영 배포 전 반드시 변경할 것)");
        }
    }

    private void initCategoriesAndMenus() {
        if (categoryRepository.count() > 0) {
            return;
        }

        Category burger = categoryRepository.save(new Category("버거", 1));
        Category side = categoryRepository.save(new Category("사이드", 2));
        Category drink = categoryRepository.save(new Category("음료", 3));

        Menu whopper = menuRepository.save(new Menu(burger, "와퍼", "불맛 그릴에 구운 100% 순쇠고기 패티",
                BigDecimal.valueOf(7100), "/images/menu/whopper.jpg", false, 100));
        Menu cheeseWhopper = menuRepository.save(new Menu(burger, "치즈와퍼", "와퍼에 체다치즈 두 장을 더한 메뉴",
                BigDecimal.valueOf(7800), "/images/menu/cheese-whopper.jpg", false, 100));
        menuRepository.save(new Menu(burger, "불고기버거", "한국인의 입맛을 사로잡은 불고기 소스 버거",
                BigDecimal.valueOf(4500), "/images/menu/bulgogi-burger.jpg", false, 100));
        Menu onionRing = menuRepository.save(new Menu(side, "어니언링", "바삭한 튀김옷의 어니언링",
                BigDecimal.valueOf(3200), "/images/menu/onion-ring.jpg", false, 100));
        Menu coke = menuRepository.save(new Menu(drink, "콜라", "탄산 음료",
                BigDecimal.valueOf(2200), "/images/menu/coke.jpg", false, 100));

        Menu whopperSet = menuRepository.save(new Menu(burger, "와퍼 세트", "와퍼 + 어니언링 + 콜라",
                BigDecimal.valueOf(9900), "/images/menu/whopper-set.jpg", true, 50));
        Menu cheeseWhopperSet = menuRepository.save(new Menu(burger, "치즈와퍼 세트", "치즈와퍼 + 어니언링 + 콜라",
                BigDecimal.valueOf(10600), "/images/menu/cheese-whopper-set.jpg", true, 50));

        setMenuItemRepository.save(new SetMenuItem(whopperSet, whopper, 1));
        setMenuItemRepository.save(new SetMenuItem(whopperSet, onionRing, 1));
        setMenuItemRepository.save(new SetMenuItem(whopperSet, coke, 1));
        setMenuItemRepository.save(new SetMenuItem(cheeseWhopperSet, cheeseWhopper, 1));
        setMenuItemRepository.save(new SetMenuItem(cheeseWhopperSet, onionRing, 1));
        setMenuItemRepository.save(new SetMenuItem(cheeseWhopperSet, coke, 1));

        log.info("초기 카테고리/메뉴 데이터 적재 완료");
    }
}
