package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.entity.enums.Role;
import com.team2.auth.command.domain.entity.enums.UserStatus;
import com.team2.auth.common.PagedResponse;
import com.team2.auth.query.dto.UserListResponse;
import com.team2.auth.query.service.UserQueryService;
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

@WebMvcTest(UserQueryController.class)
@WithMockUser
class UserQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserQueryService userQueryService;

    private UserListResponse createTestUserListResponse() {
        UserListResponse response = new UserListResponse();
        response.setUserId(1);
        response.setEmployeeNo("EMP001");
        response.setUserName("홍길동");
        response.setUserEmail("hong@test.com");
        response.setUserRole("SALES");
        response.setDepartmentName("영업부");
        response.setPositionName("팀원");
        response.setUserStatus("ACTIVE");
        return response;
    }

    private User createTestUser() {
        return User.builder()
                .employeeNo("EMP001")
                .userName("홍길동")
                .userEmail("hong@test.com")
                .userPw("encodedPassword")
                .userRole(Role.SALES)
                .userStatus(UserStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("GET /api/users - 기본 페이징으로 사용자 목록 조회")
    void getUsers_success() throws Exception {
        // given
        PagedResponse<UserListResponse> pagedResponse = PagedResponse.of(
                List.of(createTestUserListResponse()), 1L, 0, 10);
        given(userQueryService.getUsers(null, null, null, null, 0, 10))
                .willReturn(pagedResponse);

        // when & then
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userName").value("홍길동"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    @DisplayName("GET /api/users - 검색 조건과 페이징 파라미터로 사용자 목록 조회")
    void getUsers_withParams() throws Exception {
        // given
        PagedResponse<UserListResponse> pagedResponse = PagedResponse.of(
                List.of(createTestUserListResponse()), 1L, 1, 5);
        given(userQueryService.getUsers("홍길동", 1, "SALES", "ACTIVE", 1, 5))
                .willReturn(pagedResponse);

        // when & then
        mockMvc.perform(get("/api/users")
                        .param("userName", "홍길동")
                        .param("departmentId", "1")
                        .param("userRole", "SALES")
                        .param("userStatus", "ACTIVE")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].userName").value("홍길동"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.currentPage").value(1));
    }

    @Test
    @DisplayName("GET /api/users/{id} - 사용자 상세 조회")
    void getUser_success() throws Exception {
        // given
        given(userQueryService.getUser(1)).willReturn(createTestUser());

        // when & then
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("홍길동"));
    }
}
