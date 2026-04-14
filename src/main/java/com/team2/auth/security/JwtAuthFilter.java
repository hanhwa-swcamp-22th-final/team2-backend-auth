package com.team2.auth.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (!jwtProvider.validateAccessToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtProvider.parseAccessToken(token);
        Integer userId = Integer.valueOf(claims.getSubject());
        String email = claims.get("email", String.class);
        String name = claims.get("name", String.class);
        String role = claims.get("role", String.class);
        Object teamIdObj = claims.get("teamId");
        Integer teamId = teamIdObj instanceof Number ? ((Number) teamIdObj).intValue() : null;
        Object deptIdObj = claims.get("departmentId");
        Integer departmentId = deptIdObj instanceof Number ? ((Number) deptIdObj).intValue() : null;

        AuthUser authUser = new AuthUser(userId, email, name, role, teamId, departmentId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(authUser, null, authUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
