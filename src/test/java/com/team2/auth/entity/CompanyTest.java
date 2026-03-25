package com.team2.auth.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

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

        // then
        assertEquals("한화솔루션", company.getName());
        assertEquals("서울시 강남구 테스트로 123", company.getAddressKr());
        assertEquals("02-1234-5678", company.getTel());
    }

    @Test
    @DisplayName("회사 정보 수정 성공: 회사명과 연락처를 변경할 수 있다.")
    void updateCompanyInfo_Success() {
        // given
        Company company = Company.builder()
                .name("한화솔루션")
                .tel("02-1234-5678")
                .build();

        // when
        company.updateInfo("한화에너지", null, null,
                "02-9999-8888", null, null, null, null);

        // then
        assertEquals("한화에너지", company.getName());
        assertEquals("02-9999-8888", company.getTel());
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

        // when
        company.updateInfo(null, null, null, null, null, null, null, null);

        // then
        assertEquals("한화솔루션", company.getName());
        assertEquals("123 Test St, Seoul", company.getAddressEn());
        assertEquals("서울시 강남구 테스트로 123", company.getAddressKr());
        assertEquals("02-1234-5678", company.getTel());
        assertEquals("02-1234-5679", company.getFax());
        assertEquals("info@hanwha.com", company.getEmail());
        assertEquals("https://hanwha.com", company.getWebsite());
        assertEquals("https://hanwha.com/seal.png", company.getSealImageUrl());
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

        // when
        company.updateInfo("한화에너지", "456 New St, Busan", "부산시 해운대구 새로운로 456",
                "051-9999-8888", "051-9999-8889", "new@hanwha.com",
                "https://energy.hanwha.com", "https://energy.hanwha.com/seal.png");

        // then
        assertEquals("한화에너지", company.getName());
        assertEquals("456 New St, Busan", company.getAddressEn());
        assertEquals("부산시 해운대구 새로운로 456", company.getAddressKr());
        assertEquals("051-9999-8888", company.getTel());
        assertEquals("051-9999-8889", company.getFax());
        assertEquals("new@hanwha.com", company.getEmail());
        assertEquals("https://energy.hanwha.com", company.getWebsite());
        assertEquals("https://energy.hanwha.com/seal.png", company.getSealImageUrl());
    }
}
