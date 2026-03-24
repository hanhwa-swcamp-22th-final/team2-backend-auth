package com.team2.auth.entity;

import com.team2.auth.entity.enums.Role;
import com.team2.auth.entity.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "employee_no", nullable = false, unique = true, length = 20)
    private String employeeNo;

    @Column(name = "user_name", nullable = false, length = 100)
    private String name;

    @Column(name = "user_email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "user_pw", nullable = false, length = 255)
    private String pw;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public User(String employeeNo, String name, String email, String pw,
                Role role, UserStatus status) {
        this.employeeNo = employeeNo;
        this.name = name;
        this.email = email;
        this.pw = pw;
        this.role = role;
        this.status = status;
    }

    public boolean canLogin() {
        return this.status == UserStatus.재직;
    }

    public void changeStatus(UserStatus newStatus) {
        if (this.status == UserStatus.퇴직) {
            throw new IllegalStateException("퇴직한 사용자의 상태는 변경할 수 없습니다.");
        }
        this.status = newStatus;
    }

    public void assignDepartment(Department department) {
        this.department = department;
    }

    public void assignPosition(Position position) {
        this.position = position;
    }

    public boolean hasApprovalAuthority() {
        return this.position != null && this.position.hasApprovalAuthority();
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public void updateInfo(String name, String email) {
        if (name != null) this.name = name;
        if (email != null) this.email = email;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
