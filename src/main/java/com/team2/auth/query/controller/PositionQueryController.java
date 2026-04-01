package com.team2.auth.query.controller;

import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.query.service.PositionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
public class PositionQueryController {

    private final PositionQueryService positionQueryService;

    @GetMapping
    public ResponseEntity<List<Position>> getAllPositions() {
        return ResponseEntity.ok(positionQueryService.getAllPositions());
    }
}
