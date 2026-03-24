package com.team2.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.dto.CreatePositionRequest;
import com.team2.auth.entity.Position;
import com.team2.auth.service.PositionService;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PositionController.class)
@WithMockUser
class PositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PositionService positionService;

    @Test
    @DisplayName("POST /api/positions - 직급 생성 성공")
    void createPosition_success() throws Exception {
        // given
        CreatePositionRequest request = new CreatePositionRequest("사원", 5);
        given(positionService.createPosition("사원", 5)).willReturn(new Position("사원", 5));

        // when & then
        mockMvc.perform(post("/api/positions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("사원"))
                .andExpect(jsonPath("$.level").value(5));
    }

    @Test
    @DisplayName("GET /api/positions - 전체 직급 목록 조회")
    void getAllPositions_success() throws Exception {
        // given
        given(positionService.getAllPositions()).willReturn(List.of(
                new Position("사원", 5),
                new Position("대리", 4)
        ));

        // when & then
        mockMvc.perform(get("/api/positions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("사원"))
                .andExpect(jsonPath("$[1].name").value("대리"));
    }
}
