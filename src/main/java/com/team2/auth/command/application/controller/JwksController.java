package com.team2.auth.command.application.controller;

import com.nimbusds.jose.jwk.RSAKey;
import com.team2.auth.security.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Tag(name = "JWKS", description = "JSON Web Key Set 엔드포인트")
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final JwtProvider jwtProvider;

    @Operation(summary = "JWKS 조회", description = "JWT 서명 검증에 사용할 공개키 목록을 JWK Set 형식으로 반환합니다.")
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> jwks() {
        RSAKey rsaKey = new RSAKey.Builder(jwtProvider.getPublicKey())
                .keyID(jwtProvider.getKid())
                .build();

        // Build JWK map using RSA key components — avoids net.minidev transitive dep
        Map<String, Object> jwk = Map.of(
                "kty", "RSA",
                "use", "sig",
                "alg", "RS256",
                "kid", jwtProvider.getKid(),
                "n", rsaKey.getModulus().toString(),
                "e", rsaKey.getPublicExponent().toString()
        );

        Map<String, Object> jwkSet = Map.of("keys", List.of(jwk));

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/jwk-set+json"))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(jwkSet);
    }
}
