package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.CreatePositionRequest;
import com.team2.auth.command.application.dto.UpdatePositionRequest;
import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.application.service.PositionCommandService;
import com.team2.auth.query.controller.PositionQueryController;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionCommandController {

    private final PositionCommandService positionCommandService;

    @PostMapping
    public ResponseEntity<EntityModel<Position>> createPosition(@RequestBody CreatePositionRequest request) {
        Position position = positionCommandService.createPosition(request.getName(), request.getLevel());
        EntityModel<Position> model = EntityModel.of(position,
                linkTo(methodOn(PositionQueryController.class).getAllPositions()).withRel("positions"));
        URI location = linkTo(methodOn(PositionQueryController.class).getAllPositions()).toUri();
        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Position>> updatePosition(@PathVariable Integer id,
                                                    @RequestBody UpdatePositionRequest request) {
        Position position = positionCommandService.updatePosition(id, request.getName(), request.getLevel());
        return ResponseEntity.ok(EntityModel.of(position,
                linkTo(methodOn(PositionQueryController.class).getAllPositions()).withRel("positions")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Integer id) {
        positionCommandService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}
