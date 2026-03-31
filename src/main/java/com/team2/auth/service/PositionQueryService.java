package com.team2.auth.service;

import com.team2.auth.entity.Position;
import com.team2.auth.mapper.PositionQueryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PositionQueryService {

    private final PositionQueryMapper positionQueryMapper;

    public List<Position> getAllPositions() {
        return positionQueryMapper.findAll();
    }
}
