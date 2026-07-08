package com.example.kiosk_backend.security;

import com.example.kiosk_backend.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/** 관리자 JWT 발급/검증 전담 컴포넌트. payload subject는 admins.username(PK)이다. */
@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessTokenExpirationMillis;

    @Autowired
    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationMillis = jwtProperties.accessTokenExpiration();
    }

    public String createAccessToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMillis);
        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public long getAccessTokenExpirationSeconds() {
        return accessTokenExpirationMillis / 1000;
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
