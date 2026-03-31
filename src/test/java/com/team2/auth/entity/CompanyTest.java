package com.team2.auth.entity;

import com.team2.auth.repository.CompanyRepository;
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
                .name("한화솔루션")
                .addressEn("123 Test St, Seoul")
                .addressKr("서울시 강남구 테스트로 123")
                .tel("02-1234-5678")
                .fax("02-1234-5679")
                .email("info@hanwha.com")
                .website("https://hanwha.com")
                .build();

        // then - 도메인 로직 검증
        assertEquals("한화솔루션", company.getName());
        assertEquals("서울시 강남구 테스트로 123", company.getAddressKr());
        assertEquals("02-1234-5678", company.getTel());

        // DB 저장 후 재조회 검증
        companyRepository.save(company);
        entityManager.flush();
        entityManager.clear();

        Company found = companyRepository.findById(company.getId()).orElseThrow();
        assertEquals("한화솔루션", found.getName());
        assertEquals("서울시 강남구 테스트로 123", found.getAddressKr());
        assertEquals("02-1234-5678", found.getTel());
        assertEquals("123 Test St, Seoul", found.getAddressEn());
        assertEquals("02-1234-5679", found.getFax());
        assertEquals("info@hanwha.com", found.getEmail());
        assertEquals("https://hanwha.com", found.getWebsite());
    }

    @Test
    @DisplayName("회사 정보 수정 성공: 회사명과 연락처를 변경할 수 있다.")
    void updateCompanyInfo_Success() {
        // given
        Company company = Company.builder()
                .name("한화솔루션")
                .tel("02-1234-5678")
                .build();
        companyRepository.save(company);
        entityManager.flush();
        entityManager.clear();

        // when
        Company found = companyRepository.findById(company.getId()).orElseThrow();
        found.updateInfo("한화에너지", null, null,
                "02-9999-8888", null, null, null, null);

        // then
        assertEquals("한화에너지", found.getName());
        assertEquals("02-9999-8888", found.getTel());

        // DB 반영 후 재조회 검증
        entityManager.flush();
        entityManager.clear();
        Company reloaded = companyRepository.findById(company.getId()).orElseThrow();
        assertEquals("한화에너지", reloaded.getName());
        assertEquals("02-9999-8888", reloaded.getTel());
    }

    @Test
    @DisplayName("회사 정보 수정: 모든 값이 null이면 기존 값을 유지한다.")
    void updateInfo_withAllNulls_keepsOriginalValues() {
        // given
        Company company = Company.builder()
                .name("한화솔루션")
                .addressEn("123 Test St, Seoul")
                .addressKr("서울시 강남구 테스트로 123")
                .tel("02-1234-5678")
                .fax("02-1234-5679")
                .email("info@hanwha.com")
                .website("https://hanwha.com")
                .sealImageUrl("https://hanwha.com/seal.png")
                .build();
        companyRepository.save(company);
        entityManager.flush();
        entityManager.clear();

        // when
        Company found = companyRepository.findById(company.getId()).orElseThrow();
        found.updateInfo(null, null, null, null, null, null, null, null);

        // then
        assertEquals("한화솔루션", found.getName());
        assertEquals("123 Test St, Seoul", found.getAddressEn());
        assertEquals("서울시 강남구 테스트로 123", found.getAddressKr());
        assertEquals("02-1234-5678", found.getTel());
        assertEquals("02-1234-5679", found.getFax());
        assertEquals("info@hanwha.com", found.getEmail());
        assertEquals("https://hanwha.com", found.getWebsite());
        assertEquals("https://hanwha.com/seal.png", found.getSealImageUrl());

        // DB 반영 후 재조회 검증
        entityManager.flush();
        entityManager.clear();
        Company reloaded = companyRepository.findById(company.getId()).orElseThrow();
        assertEquals("한화솔루션", reloaded.getName());
        assertEquals("123 Test St, Seoul", reloaded.getAddressEn());
        assertEquals("서울시 강남구 테스트로 123", reloaded.getAddressKr());
        assertEquals("02-1234-5678", reloaded.getTel());
        assertEquals("02-1234-5679", reloaded.getFax());
        assertEquals("info@hanwha.com", reloaded.getEmail());
        assertEquals("https://hanwha.com", reloaded.getWebsite());
        assertEquals("https://hanwha.com/seal.png", reloaded.getSealImageUrl());
    }

    @Test
    @DisplayName("회사 정보 수정: 모든 값이 전달되면 모두 변경된다.")
    void updateInfo_withAllValues_updatesAll() {
        // given
        Company company = Company.builder()
                .name("한화솔루션")
                .addressEn("123 Test St, Seoul")
                .addressKr("서울시 강남구 테스트로 123")
                .tel("02-1234-5678")
                .fax("02-1234-5679")
                .email("info@hanwha.com")
                .website("https://hanwha.com")
                .sealImageUrl("https://hanwha.com/seal.png")
                .build();
        companyRepository.save(company);
        entityManager.flush();
        entityManager.clear();

        // when
        Company found = companyRepository.findById(company.getId()).orElseThrow();
        found.updateInfo("한화에너지", "456 New St, Busan", "부산시 해운대구 새로운로 456",
                "051-9999-8888", "051-9999-8889", "new@hanwha.com",
                "https://energy.hanwha.com", "https://energy.hanwha.com/seal.png");

        // then
        assertEquals("한화에너지", found.getName());
        assertEquals("456 New St, Busan", found.getAddressEn());
        assertEquals("부산시 해운대구 새로운로 456", found.getAddressKr());
        assertEquals("051-9999-8888", found.getTel());
        assertEquals("051-9999-8889", found.getFax());
        assertEquals("new@hanwha.com", found.getEmail());
        assertEquals("https://energy.hanwha.com", found.getWebsite());
        assertEquals("https://energy.hanwha.com/seal.png", found.getSealImageUrl());

        // DB 반영 후 재조회 검증
        entityManager.flush();
        entityManager.clear();
        Company reloaded = companyRepository.findById(company.getId()).orElseThrow();
        assertEquals("한화에너지", reloaded.getName());
        assertEquals("456 New St, Busan", reloaded.getAddressEn());
        assertEquals("부산시 해운대구 새로운로 456", reloaded.getAddressKr());
        assertEquals("051-9999-8888", reloaded.getTel());
        assertEquals("051-9999-8889", reloaded.getFax());
        assertEquals("new@hanwha.com", reloaded.getEmail());
        assertEquals("https://energy.hanwha.com", reloaded.getWebsite());
        assertEquals("https://energy.hanwha.com/seal.png", reloaded.getSealImageUrl());
    }
}
