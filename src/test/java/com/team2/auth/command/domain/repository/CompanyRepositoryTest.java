package com.team2.auth.repository;

import com.team2.auth.command.domain.repository.CompanyRepository;
import com.team2.auth.command.domain.entity.Company;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(excludeAutoConfiguration = MybatisAutoConfiguration.class)
class CompanyRepositoryTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("가장 먼저 생성된 회사 정보를 조회할 수 있다")
    void findTopByOrderByCompanyIdAsc() {
        // given
        Company company1 = Company.builder()
                .companyName("한화솔루션")
                .companyAddressKr("서울시 강남구")
                .build();
        entityManager.persist(company1);

        Company company2 = Company.builder()
                .companyName("한화에너지")
                .companyAddressKr("서울시 서초구")
                .build();
        entityManager.persist(company2);
        entityManager.flush();

        // when
        Optional<Company> result = companyRepository.findTopByOrderByCompanyIdAsc();

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getCompanyName()).isEqualTo("한화솔루션");
    }

    @Test
    @DisplayName("회사 정보가 없으면 빈 Optional을 반환한다")
    void findTopByOrderByCompanyIdAsc_empty() {
        // when
        Optional<Company> result = companyRepository.findTopByOrderByCompanyIdAsc();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회사 정보 수정 시 updatedAt이 갱신된다")
    void updateCompany_updatesTimestamp() {
        // given
        Company company = Company.builder()
                .companyName("한화솔루션")
                .companyAddressKr("서울시 강남구")
                .build();
        entityManager.persistAndFlush(company);

        // when
        company.updateInfo("한화에너지", null, null, null, null, null, null, null);
        entityManager.persistAndFlush(company);
        entityManager.clear();
        Company updated = companyRepository.findById(company.getCompanyId()).orElseThrow();

        // then
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getCompanyName()).isEqualTo("한화에너지");
    }
}
