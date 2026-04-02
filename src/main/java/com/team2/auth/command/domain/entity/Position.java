package com.team2.auth.command.domain.entity;

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
    private Integer positionId;

    @Column(name = "position_name", nullable = false, length = 50)
    private String positionName;

    @Column(name = "position_level", nullable = false)
    private int positionLevel;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Position(String positionName, int positionLevel) {
        if (positionName == null || positionName.isBlank()) {
            throw new IllegalArgumentException("직급명은 필수입니다.");
        }
        this.positionName = positionName;
        this.positionLevel = positionLevel;
    }

    public void updateInfo(String positionName, int positionLevel) {
        this.positionName = positionName;
        this.positionLevel = positionLevel;
    }

    public boolean hasApprovalAuthority() {
        return this.positionLevel == 1;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
