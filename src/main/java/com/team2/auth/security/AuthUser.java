package com.team2.auth.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser implements UserDetails {

    private final Integer userId;
    private final String email;
    private final String name;
    private final String role;
    private final Integer teamId;
    private final Integer departmentId;
    private final List<GrantedAuthority> authorities;

    public AuthUser(Integer userId, String email, String name, String role,
                    Integer teamId, Integer departmentId) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.teamId = teamId;
        this.departmentId = departmentId;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
