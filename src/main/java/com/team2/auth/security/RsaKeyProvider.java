package com.team2.auth.security;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
@Getter
public class RsaKeyProvider {

    @Value("${jwt.rsa.private-key-path}")
    private String privateKeyPath;

    @Value("${jwt.rsa.public-key-path}")
    private String publicKeyPath;

    private final ResourceLoader resourceLoader;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public RsaKeyProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            this.privateKey = loadPrivateKey(privateKeyPath);
            this.publicKey = loadPublicKey(publicKeyPath);
        } catch (Exception e) {
            throw new IllegalStateException("RSA key loading failed — application cannot start: " + e.getMessage(), e);
        }
    }

    private RSAPrivateKey loadPrivateKey(String path) throws Exception {
        String pem = readResource(path)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private RSAPublicKey loadPublicKey(String path) throws Exception {
        String pem = readResource(path)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decoded));
    }

    private String readResource(String path) throws Exception {
        Resource resource = resourceLoader.getResource(path);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes());
        }
    }
}
