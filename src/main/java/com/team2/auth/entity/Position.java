package com.team2.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "positions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Position {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "position_id")
    private Integer id;

    @Column(name = "position_name", nullable = false, length = 50)
    private String name;

    @Column(name = "position_level", nullable = false)
    private int level;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Position(String name, int level) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("직급명은 필수입니다.");
        }
        this.name = name;
        this.level = level;
    }

    public boolean hasApprovalAuthority() {
        return this.level == 1;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
