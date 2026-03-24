package com.team2.auth.controller;

import com.team2.auth.dto.CreatePositionRequest;
import com.team2.auth.entity.Position;
import com.team2.auth.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    public ResponseEntity<Position> createPosition(@RequestBody CreatePositionRequest request) {
        Position position = positionService.createPosition(request.getName(), request.getLevel());
        return ResponseEntity.status(HttpStatus.CREATED).body(position);
    }

    @GetMapping
    public ResponseEntity<List<Position>> getAllPositions() {
        return ResponseEntity.ok(positionService.getAllPositions());
    }
}
