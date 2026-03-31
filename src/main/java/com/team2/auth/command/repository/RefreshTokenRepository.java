package com.team2.auth.command.repository;

import com.team2.auth.entity.RefreshToken;
import com.team2.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByTokenValue(String tokenValue);

    Optional<RefreshToken> findByUser(User user);

    void deleteByUser(User user);
}
