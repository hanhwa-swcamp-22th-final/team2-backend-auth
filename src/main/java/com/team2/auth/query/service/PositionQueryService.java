package com.team2.auth.query.service;

import com.team2.auth.entity.Position;
import com.team2.auth.query.mapper.PositionQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionQueryService {

    private final PositionQueryMapper positionQueryMapper;

    public Position getPosition(Integer id) {
        Position position = positionQueryMapper.findById(id);
        if (position == null) {
            throw new IllegalArgumentException("직급을 찾을 수 없습니다.");
        }
        return position;
    }

    public List<Position> getAllPositions() {
        return positionQueryMapper.findAll();
    }

    public Position getPositionByName(String name) {
        Position position = positionQueryMapper.findByPositionName(name);
        if (position == null) {
            throw new IllegalArgumentException("직급을 찾을 수 없습니다.");
        }
        return position;
    }
}
