package com.team2.auth.query.controller;

import com.team2.auth.entity.Department;
import com.team2.auth.query.service.DepartmentQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DepartmentQueryController.class)
@WithMockUser
class DepartmentQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepartmentQueryService departmentQueryService;

    @Test
    @DisplayName("GET /api/departments - 전체 부서 목록 조회")
    void getAllDepartments_success() throws Exception {
        // given
        given(departmentQueryService.getAllDepartments()).willReturn(List.of(
                new Department("영업부"),
                new Department("생산부")
        ));

        // when & then
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].departmentName").value("영업부"))
                .andExpect(jsonPath("$[1].departmentName").value("생산부"));
    }
}
