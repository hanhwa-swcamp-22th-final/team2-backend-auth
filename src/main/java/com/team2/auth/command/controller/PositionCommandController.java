package com.team2.auth.command.controller;

import com.team2.auth.dto.CreatePositionRequest;
import com.team2.auth.entity.Position;
import com.team2.auth.command.service.PositionCommandService;
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
}
