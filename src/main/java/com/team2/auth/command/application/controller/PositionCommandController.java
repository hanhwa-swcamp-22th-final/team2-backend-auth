package com.team2.auth.command.application.controller;

import com.team2.auth.command.application.dto.CreatePositionRequest;
import com.team2.auth.command.application.dto.UpdatePositionRequest;
import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.application.service.PositionCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionCommandController {

    private final PositionCommandService positionCommandService;

    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody CreatePositionRequest request) {
        Position position = positionCommandService.createPosition(request.getName(), request.getLevel());
        return ResponseEntity.status(HttpStatus.CREATED).body(position);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Position> updatePosition(@PathVariable Integer id,
                                                    @RequestBody UpdatePositionRequest request) {
        return ResponseEntity.ok(positionCommandService.updatePosition(id, request.getName(), request.getLevel()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePosition(@PathVariable Integer id) {
        positionCommandService.deletePosition(id);
        return ResponseEntity.noContent().build();
    }
}
