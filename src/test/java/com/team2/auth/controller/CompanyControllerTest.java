package com.team2.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.dto.UpdateCompanyRequest;
import com.team2.auth.entity.Company;
import com.team2.auth.service.CompanyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyController.class)
@WithMockUser
class CompanyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyService companyService;

    @Test
    @DisplayName("GET /api/company - 회사 정보 조회 성공")
    void getCompany_success() throws Exception {
        // given
        Company company = Company.builder()
                .name("Team2 Corp")
                .addressKr("서울시 강남구")
                .tel("02-1234-5678")
                .build();
        given(companyService.getCompany()).willReturn(company);

        // when & then
        mockMvc.perform(get("/api/company"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Team2 Corp"))
                .andExpect(jsonPath("$.addressKr").value("서울시 강남구"));
    }

    @Test
    @DisplayName("PUT /api/company - 회사 정보 수정 성공")
    void updateCompany_success() throws Exception {
        // given
        UpdateCompanyRequest request = UpdateCompanyRequest.builder()
                .name("Team2 Updated")
                .tel("02-9999-8888")
                .build();
        Company updated = Company.builder()
                .name("Team2 Updated")
                .tel("02-9999-8888")
                .build();
        given(companyService.updateCompany(any(UpdateCompanyRequest.class))).willReturn(updated);

        // when & then
        mockMvc.perform(put("/api/company")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Team2 Updated"));
    }
}
