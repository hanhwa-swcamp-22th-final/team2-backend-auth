package com.team2.auth.security;

import com.team2.auth.command.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;
    private final String kid;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;
    private final String issuer;

    public JwtProvider(
            RsaKeyProvider rsaKeyProvider,
            @Value("${jwt.access-token-expiry:3600000}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry:604800000}") long refreshTokenExpiry,
            @Value("${jwt.issuer:team2-auth}") String issuer) {
        this.privateKey = rsaKeyProvider.getPrivateKey();
        this.publicKey = rsaKeyProvider.getPublicKey();
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.issuer = issuer;
        this.kid = computeKid(this.publicKey);
    }

    // Test constructor: accepts keys directly (no RsaKeyProvider needed)
    public JwtProvider(RSAPrivateKey privateKey, RSAPublicKey publicKey, long accessTokenExpiry,
                       long refreshTokenExpiry, String issuer) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.accessTokenExpiry = accessTokenExpiry;
        this.refreshTokenExpiry = refreshTokenExpiry;
        this.issuer = issuer;
        this.kid = computeKid(this.publicKey);
    }

    private static String computeKid(RSAPublicKey publicKey) {
        try {
            byte[] encoded = publicKey.getEncoded();
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(encoded);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute key fingerprint", e);
        }
    }

    public String getKid() {
        return kid;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public String generateAccessToken(User user) {
        Date now = new Date();
        Integer departmentId = user.getDepartment() != null ? user.getDepartment().getDepartmentId() : null;
        return Jwts.builder()
                .header().keyId(kid).and()
                .issuer(issuer)
                .subject(String.valueOf(user.getUserId()))
                .claim("email", user.getUserEmail())
                .claim("name", user.getUserName())
                .claim("role", user.getUserRole().name())
                .claim("departmentId", departmentId)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + accessTokenExpiry))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    public long getRefreshTokenExpiry() {
        return refreshTokenExpiry;
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateAccessToken(String token) {
        try {
            parseAccessToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
