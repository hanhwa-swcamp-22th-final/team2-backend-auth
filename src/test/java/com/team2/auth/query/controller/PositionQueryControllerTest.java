package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.query.service.PositionQueryService;
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

@WebMvcTest(PositionQueryController.class)
@WithMockUser
class PositionQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PositionQueryService positionQueryService;

    @Test
    @DisplayName("GET /api/positions - 전체 직급 목록 조회")
    void getAllPositions_success() throws Exception {
        // given
        given(positionQueryService.getAllPositions()).willReturn(List.of(
                new Position("팀장", 1),
                new Position("팀원", 2)
        ));

        // when & then
        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].positionName").value("팀장"))
                .andExpect(jsonPath("$[1].positionName").value("팀원"));
    }
}
