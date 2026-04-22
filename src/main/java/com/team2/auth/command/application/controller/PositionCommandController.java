package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.CreatePositionRequest;
import com.team2.auth.command.application.dto.UpdatePositionRequest;
import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.application.service.PositionCommandService;
import com.team2.auth.query.controller.PositionQueryController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "직급 관리 (Command)", description = "직급 생성, 수정, 삭제 API")
@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionCommandController {

    private final PositionCommandService positionCommandService;

    @Operation(summary = "직급 생성", description = "새로운 직급을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "직급 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (중복된 직급명 등)")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Position>> createPosition(@Valid @RequestBody CreatePositionRequest request) {
        Position position = positionCommandService.createPosition(request.getName(), request.getLevel());
        EntityModel<Position> model = EntityModel.of(position,
                linkTo(methodOn(PositionQueryController.class).getAllPositions()).withRel("positions"));
        URI location = linkTo(methodOn(PositionQueryController.class).getAllPositions()).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @Operation(summary = "직급 수정", description = "기존 직급의 이름과 레벨을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "직급 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "404", description = "직급을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Position>> updatePosition(
            @Parameter(description = "직급 ID") @PathVariable("id") Integer id,
            @Valid @RequestBody UpdatePositionRequest request) {
        Position position = positionCommandService.updatePosition(id, request.getName(), request.getLevel());
        return ResponseEntity.ok(EntityModel.of(position,
                linkTo(methodOn(PositionQueryController.class).getAllPositions()).withRel("positions")));
    }

    @Operation(summary = "직급 삭제", description = "직급을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "직급 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "직급을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(
            @Parameter(description = "직급 ID") @PathVariable("id") Integer id) {
        positionCommandService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}
