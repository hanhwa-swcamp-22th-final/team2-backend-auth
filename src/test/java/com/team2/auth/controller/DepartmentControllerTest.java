package com.team2.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.dto.CreateDepartmentRequest;
import com.team2.auth.entity.Department;
import com.team2.auth.service.DepartmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentController.class)
@WithMockUser
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    @Test
    @DisplayName("POST /api/departments - 부서 생성 성공")
    void createDepartment_success() throws Exception {
        // given
        CreateDepartmentRequest request = new CreateDepartmentRequest("영업부");
        given(departmentService.createDepartment("영업부")).willReturn(new Department("영업부"));

        // when & then
        mockMvc.perform(post("/api/departments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("영업부"));
    }

    @Test
    @DisplayName("GET /api/departments - 전체 부서 목록 조회")
    void getAllDepartments_success() throws Exception {
        // given
        given(departmentService.getAllDepartments()).willReturn(List.of(
                new Department("영업부"),
                new Department("생산부")
        ));

        // when & then
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("영업부"))
                .andExpect(jsonPath("$[1].name").value("생산부"));
    }

    @Test
    @DisplayName("DELETE /api/departments/{id} - 부서 삭제 성공")
    void deleteDepartment_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/departments/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(departmentService).deleteDepartment(1);
    }
}
