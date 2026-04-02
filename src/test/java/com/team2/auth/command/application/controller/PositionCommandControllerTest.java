package com.team2.auth.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team2.auth.command.application.dto.CreatePositionRequest;
import com.team2.auth.command.application.dto.UpdatePositionRequest;
import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.application.service.PositionCommandService;
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

@WebMvcTest(PositionCommandController.class)
@WithMockUser
class PositionCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PositionCommandService positionCommandService;

    @Test
    @DisplayName("POST /api/positions - 직급 생성 성공")
    void createPosition_success() throws Exception {
        // given
        CreatePositionRequest request = new CreatePositionRequest("팀원", 2);
        given(positionCommandService.createPosition("팀원", 2)).willReturn(new Position("팀원", 2));

        // when & then
        mockMvc.perform(post("/api/positions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.positionName").value("팀원"))
                .andExpect(jsonPath("$.positionLevel").value(2));
    }

    @Test
    @DisplayName("PUT /api/positions/{id} - 직급 수정 성공")
    void updatePosition_success() throws Exception {
        // given
        UpdatePositionRequest request = new UpdatePositionRequest("팀장", 3);
        given(positionCommandService.updatePosition(eq(1), eq("팀장"), eq(3)))
                .willReturn(new Position("팀장", 3));

        // when & then
        mockMvc.perform(put("/api/positions/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.positionName").value("팀장"))
                .andExpect(jsonPath("$.positionLevel").value(3));
    }

    @Test
    @DisplayName("DELETE /api/positions/{id} - 직급 삭제 성공")
    void deletePosition_success() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/positions/1")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(positionCommandService).deletePosition(1);
    }
}
