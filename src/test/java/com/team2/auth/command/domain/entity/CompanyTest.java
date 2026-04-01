package com.team2.auth.command.domain.entity;

import com.team2.auth.command.domain.repository.CompanyRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ImportAutoConfiguration(exclude = MybatisAutoConfiguration.class)
class CompanyTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("회사 정보 생성 성공: 필수 필드가 정상 설정된다.")
    void createCompany_Success() {
        // given & when
        Company company = Company.builder()
                .companyName("한화솔루션")
                .companyAddressEn("123 Test St, Seoul")
                .companyAddressKr("서울시 강남구 테스트로 123")
                .companyTel("02-1234-5678")
                .companyFax("02-1234-5679")
                .companyEmail("info@hanwha.com")
                .companyWebsite("https://hanwha.com")
                .build();

        // then - 도메인 로직 검증
        assertEquals("한화솔루션", company.getCompanyName());
        assertEquals("서울시 강남구 테스트로 123", company.getCompanyAddressKr());
        assertEquals("02-1234-5678", company.getCompanyTel());

        // DB 저장 후 재조회 검증
        companyRepository.save(company);
        entityManager.flush();
        entityManager.clear();

        Company found = companyRepository.findById(company.getCompanyId()).orElseThrow();
        assertEquals("한화솔루션", found.getCompanyName());
        assertEquals("서울시 강남구 테스트로 123", found.getCompanyAddressKr());
        assertEquals("02-1234-5678", found.getCompanyTel());
        assertEquals("123 Test St, Seoul", found.getCompanyAddressEn());
        assertEquals("02-1234-5679", found.getCompanyFax());
        assertEquals("info@hanwha.com", found.getCompanyEmail());
        assertEquals("https://hanwha.com", found.getCompanyWebsite());
    }

    @Test
    @DisplayName("회사 정보 수정 성공: 회사명과 연락처를 변경할 수 있다.")
    void updateCompanyInfo_Success() {
        // given
        Company company = Company.builder()
                .companyName("한화솔루션")
                .companyTel("02-1234-5678")
                .build();
        companyRepository.save(company);
        entityManager.flush();
        entityManager.clear();

        // when
        Company found = companyRepository.findById(company.getCompanyId()).orElseThrow();
        found.updateInfo("한화에너지", null, null,
                "02-9999-8888", null, null, null, null);

        // then
        assertEquals("한화에너지", found.getCompanyName());
        assertEquals("02-9999-8888", found.getCompanyTel());

        // DB 반영 후 재조회 검증
        entityManager.flush();
        entityManager.clear();
        Company reloaded = companyRepository.findById(company.getCompanyId()).orElseThrow();
        assertEquals("한화에너지", reloaded.getCompanyName());
        assertEquals("02-9999-8888", reloaded.getCompanyTel());
    }

    @Test
    @DisplayName("회사 정보 수정: 모든 값이 null이면 기존 값을 유지한다.")
    void updateInfo_withAllNulls_keepsOriginalValues() {
        // given
        Company company = Company.builder()
                .companyName("한화솔루션")
                .companyAddressEn("123 Test St, Seoul")
                .companyAddressKr("서울시 강남구 테스트로 123")
                .companyTel("02-1234-5678")
                .companyFax("02-1234-5679")
                .companyEmail("info@hanwha.com")
                .companyWebsite("https://hanwha.com")
                .companySealImageUrl("https://hanwha.com/seal.png")
                .build();
        companyRepository.save(company);
        entityManager.flush();
        entityManager.clear();

        // when
        Company found = companyRepository.findById(company.getCompanyId()).orElseThrow();
        found.updateInfo(null, null, null, null, null, null, null, null);

        // then
        assertEquals("한화솔루션", found.getCompanyName());
        assertEquals("123 Test St, Seoul", found.getCompanyAddressEn());
        assertEquals("서울시 강남구 테스트로 123", found.getCompanyAddressKr());
        assertEquals("02-1234-5678", found.getCompanyTel());
        assertEquals("02-1234-5679", found.getCompanyFax());
        assertEquals("info@hanwha.com", found.getCompanyEmail());
        assertEquals("https://hanwha.com", found.getCompanyWebsite());
        assertEquals("https://hanwha.com/seal.png", found.getCompanySealImageUrl());

        // DB 반영 후 재조회 검증
        entityManager.flush();
        entityManager.clear();
        Company reloaded = companyRepository.findById(company.getCompanyId()).orElseThrow();
        assertEquals("한화솔루션", reloaded.getCompanyName());
        assertEquals("123 Test St, Seoul", reloaded.getCompanyAddressEn());
        assertEquals("서울시 강남구 테스트로 123", reloaded.getCompanyAddressKr());
        assertEquals("02-1234-5678", reloaded.getCompanyTel());
        assertEquals("02-1234-5679", reloaded.getCompanyFax());
        assertEquals("info@hanwha.com", reloaded.getCompanyEmail());
        assertEquals("https://hanwha.com", reloaded.getCompanyWebsite());
        assertEquals("https://hanwha.com/seal.png", reloaded.getCompanySealImageUrl());
    }

    @Test
    @DisplayName("회사 정보 수정: 모든 값이 전달되면 모두 변경된다.")
    void updateInfo_withAllValues_updatesAll() {
        // given
        Company company = Company.builder()
                .companyName("한화솔루션")
                .companyAddressEn("123 Test St, Seoul")
                .companyAddressKr("서울시 강남구 테스트로 123")
                .companyTel("02-1234-5678")
                .companyFax("02-1234-5679")
                .companyEmail("info@hanwha.com")
                .companyWebsite("https://hanwha.com")
                .companySealImageUrl("https://hanwha.com/seal.png")
                .build();
        companyRepository.save(company);
        entityManager.flush();
        entityManager.clear();

        // when
        Company found = companyRepository.findById(company.getCompanyId()).orElseThrow();
        found.updateInfo("한화에너지", "456 New St, Busan", "부산시 해운대구 새로운로 456",
                "051-9999-8888", "051-9999-8889", "new@hanwha.com",
                "https://energy.hanwha.com", "https://energy.hanwha.com/seal.png");

        // then
        assertEquals("한화에너지", found.getCompanyName());
        assertEquals("456 New St, Busan", found.getCompanyAddressEn());
        assertEquals("부산시 해운대구 새로운로 456", found.getCompanyAddressKr());
        assertEquals("051-9999-8888", found.getCompanyTel());
        assertEquals("051-9999-8889", found.getCompanyFax());
        assertEquals("new@hanwha.com", found.getCompanyEmail());
        assertEquals("https://energy.hanwha.com", found.getCompanyWebsite());
        assertEquals("https://energy.hanwha.com/seal.png", found.getCompanySealImageUrl());

        // DB 반영 후 재조회 검증
        entityManager.flush();
        entityManager.clear();
        Company reloaded = companyRepository.findById(company.getCompanyId()).orElseThrow();
        assertEquals("한화에너지", reloaded.getCompanyName());
        assertEquals("456 New St, Busan", reloaded.getCompanyAddressEn());
        assertEquals("부산시 해운대구 새로운로 456", reloaded.getCompanyAddressKr());
        assertEquals("051-9999-8888", reloaded.getCompanyTel());
        assertEquals("051-9999-8889", reloaded.getCompanyFax());
        assertEquals("new@hanwha.com", reloaded.getCompanyEmail());
        assertEquals("https://energy.hanwha.com", reloaded.getCompanyWebsite());
        assertEquals("https://energy.hanwha.com/seal.png", reloaded.getCompanySealImageUrl());
    }
}
