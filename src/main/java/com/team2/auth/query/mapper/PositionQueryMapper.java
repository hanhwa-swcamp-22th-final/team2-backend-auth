package com.team2.auth.query.mapper;

import com.team2.auth.command.domain.entity.Position;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PositionQueryMapper {

    Position findById(@Param("positionId") Integer positionId);

    List<Position> findAll();

    Position findByPositionName(@Param("positionName") String positionName);
}
