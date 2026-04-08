package com.team2.auth.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.command.application.dto.UpdateCompanyRequest;
import com.team2.auth.command.domain.entity.Company;
import com.team2.auth.command.application.service.CompanyCommandService;
import com.team2.auth.security.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CompanyCommandController.class)
@WithMockUser
class CompanyCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CompanyCommandService companyCommandService;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("PUT /api/company - 회사 정보 수정 성공")
    void updateCompany_success() throws Exception {
        // given
        UpdateCompanyRequest request = UpdateCompanyRequest.builder()
                .name("Team2 Updated")
                .tel("02-9999-8888")
                .build();
        Company updated = Company.builder()
                .companyName("Team2 Updated")
                .companyTel("02-9999-8888")
                .build();
        given(companyCommandService.updateCompany(any(UpdateCompanyRequest.class))).willReturn(updated);

        // when & then
        mockMvc.perform(put("/api/company")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Team2 Updated"));
    }

    @Test
    @DisplayName("POST /api/company/seal - 직인 이미지 업로드 성공")
    void uploadSeal_success() throws Exception {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file", "seal.png", "image/png", "fake-image-content".getBytes());
        given(companyCommandService.uploadSealImage(any())).willReturn("https://s3.example.com/seal.png");

        // when & then
        mockMvc.perform(multipart("/api/company/seal")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companySealImageUrl").value("https://s3.example.com/seal.png"))
                .andExpect(jsonPath("$.message").value("업로드 완료"));
    }
}
