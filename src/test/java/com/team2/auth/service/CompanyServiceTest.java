package com.team2.auth.service;

import com.team2.auth.dto.UpdateCompanyRequest;
import com.team2.auth.entity.Company;
import com.team2.auth.repository.CompanyRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
class CompanyServiceTest {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        companyRepository.saveAndFlush(Company.builder()
                .companyName("Team2 Corp")
                .companyAddressKr("서울시 강남구")
                .companyAddressEn("Gangnam, Seoul")
                .companyTel("02-1234-5678")
                .build());
        entityManager.clear();
    }

    @Test
    @DisplayName("회사 정보를 조회할 수 있다")
    void getCompany_success() {
        Company result = companyService.getCompany();

        assertThat(result.getCompanyName()).isEqualTo("Team2 Corp");
        assertThat(result.getCompanyAddressKr()).isEqualTo("서울시 강남구");
    }

    @Test
    @DisplayName("회사 정보가 없으면 예외가 발생한다")
    void getCompany_notFound() {
        companyRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> companyService.getCompany())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회사 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("회사 정보를 수정할 수 있다 (선택적 업데이트)")
    void updateCompany_success() {
        // given
        UpdateCompanyRequest request = UpdateCompanyRequest.builder()
                .name("Team2 Updated")
                .tel("02-9999-8888")
                .build();

        // when
        Company result = companyService.updateCompany(request);
        entityManager.flush();
        entityManager.clear();

        // then - DB에서 다시 조회해서 확인
        Company updated = companyRepository.findTopByOrderByCompanyIdAsc().orElseThrow();
        assertThat(updated.getCompanyName()).isEqualTo("Team2 Updated");
        assertThat(updated.getCompanyTel()).isEqualTo("02-9999-8888");
        // null로 보낸 필드는 기존 값 유지
        assertThat(updated.getCompanyAddressKr()).isEqualTo("서울시 강남구");
    }
}
