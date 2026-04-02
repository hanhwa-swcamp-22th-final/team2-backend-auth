package com.team2.auth.command.application.service;

import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.domain.entity.User;
import com.team2.auth.command.domain.repository.PositionRepository;
import com.team2.auth.query.service.UserQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionCommandService {

    private final PositionRepository positionRepository;
    private final UserQueryService userQueryService;

    public Position createPosition(String name, int level) {
        return positionRepository.save(new Position(name, level));
    }

    public Position updatePosition(Integer id, String name, int level) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다."));
        position.updateInfo(name, level);
        return position;
    }

    public void deletePosition(Integer id) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다."));
        positionRepository.delete(position);
    }
}
