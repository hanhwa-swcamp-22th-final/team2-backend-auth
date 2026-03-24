package com.team2.auth.service;

import com.team2.auth.entity.Position;
import com.team2.auth.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionService {

    private final PositionRepository positionRepository;

    @Transactional
    public Position createPosition(String name, int level) {
        return positionRepository.save(new Position(name, level));
    }

    public List<Position> getAllPositions() {
        return positionRepository.findAll();
    }
}
