package com.team2.auth.service;

import com.team2.auth.dto.UpdateCompanyRequest;
import com.team2.auth.entity.Company;
import com.team2.auth.repository.CompanyRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    @DisplayName("회사 정보를 조회할 수 있다")
    void getCompany_success() {
        // given
        Company company = Company.builder()
                .name("Team2 Corp")
                .addressKr("서울시 강남구")
                .build();
        given(companyRepository.findTopByOrderByIdAsc()).willReturn(Optional.of(company));

        // when
        Company result = companyService.getCompany();

        // then
        assertThat(result.getName()).isEqualTo("Team2 Corp");
    }

    @Test
    @DisplayName("회사 정보가 없으면 예외가 발생한다")
    void getCompany_notFound() {
        // given
        given(companyRepository.findTopByOrderByIdAsc()).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> companyService.getCompany())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("회사 정보를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("회사 정보를 수정할 수 있다")
    void updateCompany_success() {
        // given
        Company company = Company.builder()
                .name("Team2 Corp")
                .addressKr("서울시 강남구")
                .build();
        given(companyRepository.findTopByOrderByIdAsc()).willReturn(Optional.of(company));

        UpdateCompanyRequest request = UpdateCompanyRequest.builder()
                .name("Team2 Updated")
                .tel("02-9999-8888")
                .build();

        // when
        Company result = companyService.updateCompany(request);

        // then
        assertThat(result.getName()).isEqualTo("Team2 Updated");
        assertThat(result.getTel()).isEqualTo("02-9999-8888");
    }
}
