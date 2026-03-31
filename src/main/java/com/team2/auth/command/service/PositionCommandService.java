package com.team2.auth.command.service;

import com.team2.auth.entity.Position;
import com.team2.auth.command.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionCommandService {

    private final PositionRepository positionRepository;

    public Position createPosition(String name, int level) {
        return positionRepository.save(new Position(name, level));
    }
}
