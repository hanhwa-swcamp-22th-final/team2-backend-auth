package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.query.service.PositionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionQueryController {

    private final PositionQueryService positionQueryService;

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
