package com.team2.auth.command.application.service;

import com.team2.auth.command.domain.entity.Position;
import com.team2.auth.command.domain.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PositionCommandService {

    private final PositionRepository positionRepository;

    public Position createPosition(String name, int level) {
        String normalizedName = normalizeFixedPositionName(name);
        int fixedLevel = fixedLevel(normalizedName);
        return positionRepository.findFirstByPositionNameOrderByPositionIdAsc(normalizedName)
                .map(position -> {
                    position.updateInfo(normalizedName, fixedLevel);
                    return position;
                })
                .orElseGet(() -> positionRepository.save(new Position(normalizedName, fixedLevel)));
    }

    public Position updatePosition(Integer id, String name, int level) {
        String normalizedName = normalizeFixedPositionName(name);
        int fixedLevel = fixedLevel(normalizedName);
        positionRepository.findFirstByPositionNameOrderByPositionIdAsc(normalizedName)
                .filter(position -> !position.getPositionId().equals(id))
                .ifPresent(position -> {
                    throw new IllegalArgumentException("이미 같은 이름의 직급이 존재합니다.");
                });

        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("직급을 찾을 수 없습니다."));
        position.updateInfo(normalizedName, fixedLevel);
        return position;
    }

    public void deletePosition(Integer id) {
        if (!positionRepository.existsById(id)) {
            throw new IllegalArgumentException("직급을 찾을 수 없습니다.");
        }
        throw new IllegalArgumentException("직급은 팀장/팀원 고정값이라 삭제할 수 없습니다.");
    }

    private String normalizeFixedPositionName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("직급명을 입력해주세요.");
        }

        String normalized = name.trim();
        if (!"팀장".equals(normalized) && !"팀원".equals(normalized)) {
            throw new IllegalArgumentException("직급은 팀장/팀원만 사용할 수 있습니다.");
        }
        return normalized;
    }

    private int fixedLevel(String name) {
        return "팀장".equals(name) ? 1 : 3;
    }
}
