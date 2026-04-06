package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.query.service.PositionQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Tag(name = "직급 조회", description = "직급 목록 조회 API")
@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionQueryController {

    private final PositionQueryService positionQueryService;

    @Operation(summary = "전체 직급 목록 조회", description = "등록된 모든 직급 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<Position>>> getAllPositions() {
        List<EntityModel<Position>> positions = positionQueryService.getAllPositions().stream()
                .map(p -> EntityModel.of(p,
                        linkTo(methodOn(PositionQueryController.class).getAllPositions()).withSelfRel()))
                .toList();
        return ResponseEntity.ok(CollectionModel.of(positions,
                linkTo(methodOn(PositionQueryController.class).getAllPositions()).withSelfRel()));
    }
}
