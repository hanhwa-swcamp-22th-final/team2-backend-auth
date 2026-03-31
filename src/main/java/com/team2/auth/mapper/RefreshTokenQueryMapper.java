package com.team2.auth.mapper;

import com.team2.auth.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RefreshTokenQueryMapper {

    RefreshToken findByTokenValue(@Param("tokenValue") String tokenValue);

    RefreshToken findByUserId(@Param("userId") Integer userId);
}
