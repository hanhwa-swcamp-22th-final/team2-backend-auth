package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.query.service.CompanyQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyQueryController.class)
@WithMockUser
class CompanyQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CompanyQueryService companyQueryService;

    @Test
    @DisplayName("GET /api/company - 회사 정보 조회 성공")
    void getCompany_success() throws Exception {
        // given
        Company company = Company.builder()
                .companyName("Team2 Corp")
                .companyAddressKr("서울시 강남구")
                .companyTel("02-1234-5678")
                .build();
        given(companyQueryService.getCompany()).willReturn(company);

        // when & then
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Team2 Corp"))
                .andExpect(jsonPath("$.companyAddressKr").value("서울시 강남구"));
    }
}
