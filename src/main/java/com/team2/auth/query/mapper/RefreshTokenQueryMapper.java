package com.team2.auth.query.mapper;

import com.team2.auth.command.domain.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenQueryMapper {

    RefreshToken findByTokenValue(@Param("tokenValue") String tokenValue);

    RefreshToken findByUserId(@Param("userId") Integer userId);
}
