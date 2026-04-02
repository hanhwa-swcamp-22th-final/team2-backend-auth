package com.team2.auth.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.command.application.dto.CreateDepartmentRequest;
import com.team2.auth.command.application.dto.UpdateDepartmentRequest;
import com.team2.auth.command.domain.entity.Department;
import com.team2.auth.command.application.service.DepartmentCommandService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentCommandController.class)
@WithMockUser
class DepartmentCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentCommandService departmentCommandService;

    @Test
    @DisplayName("POST /api/departments - 부서 생성 성공")
    void createDepartment_success() throws Exception {
        // given
        CreateDepartmentRequest request = new CreateDepartmentRequest("영업부");
        given(departmentCommandService.createDepartment("영업부")).willReturn(new Department("영업부"));

        // when & then
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.departmentName").value("영업부"));
    }

    @Test
    @DisplayName("DELETE /api/departments/{id} - 부서 삭제 성공")
    void deleteDepartment_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/departments/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(departmentCommandService).deleteDepartment(1);
    }

    @Test
    @DisplayName("PUT /api/departments/{id} - 부서 수정 성공")
    void updateDepartment_success() throws Exception {
        // given
        UpdateDepartmentRequest request = new UpdateDepartmentRequest("기획부");
        given(departmentCommandService.updateDepartment(eq(1), eq("기획부")))
                .willReturn(new Department("기획부"));

        // when & then
        mockMvc.perform(put("/api/departments/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departmentName").value("기획부"));
    }
}
