package com.team2.auth.command.domain.entity;

import com.team2.auth.command.domain.entity.converter.RoleConverter;
import com.team2.auth.command.domain.entity.converter.UserStatusConverter;
import com.team2.auth.command.domain.entity.enums.Role;
import com.team2.auth.command.domain.entity.enums.UserStatus;
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
    private Integer userId;

    @Column(name = "employee_no", nullable = false, unique = true, length = 20)
    private String employeeNo;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Column(name = "user_email", nullable = false, unique = true, length = 255)
    private String userEmail;

    @Column(name = "user_pw", nullable = false, length = 255)
    private String userPw;

    @Convert(converter = RoleConverter.class)
    @Column(name = "user_role", nullable = false)
    private Role userRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Convert(converter = UserStatusConverter.class)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public User(String employeeNo, String userName, String userEmail, String userPw,
                Role userRole, UserStatus userStatus) {
        this.employeeNo = employeeNo;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPw = userPw;
        this.userRole = userRole;
        this.userStatus = userStatus;
    }

    public boolean canLogin() {
        return this.userStatus == UserStatus.ACTIVE;
    }

    public void changeStatus(UserStatus newStatus) {
        if (this.userStatus == UserStatus.RETIRED) {
            throw new IllegalStateException("퇴직한 사용자의 상태는 변경할 수 없습니다.");
        }
        this.userStatus = newStatus;
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
        return this.userRole == Role.ADMIN;
    }

    public void updateInfo(String userName, String userEmail) {
        if (userName != null) this.userName = userName;
        if (userEmail != null) this.userEmail = userEmail;
    }

    public void changePassword(String encodedPassword) {
        this.userPw = encodedPassword;
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
