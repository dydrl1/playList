package com.playlist.backend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private final Key key;
    private final long accessTokenExpirationMillis;
    private final UserDetailsService userDetailsService;

    public JwtUtil(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationMillis,
            UserDetailsService userDetailsService
    ) {
        // Base64 설정
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessTokenExpirationMillis = accessTokenExpirationMillis;
        this.userDetailsService = userDetailsService;
    }

    /* ===================== 토큰 생성 ===================== */

    public String generateAccessToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpirationMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /* ===================== 토큰 검증 / 파싱 ===================== */

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 형식 오류 등
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getBody().getSubject();
    }

    private Jws<Claims> parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }

    /* ===================== Spring Security 연동 ===================== */

    public Authentication getAuthentication(String token) {
        String username = getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        return new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
    }

    /* ===================== HTTP 요청/응답 편의 메서드 ===================== */

    /** 요청 헤더에서 Bearer 토큰 추출 */
    public String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    /** 응답에 Authorization: Bearer <token> 헤더까지 같이 세팅 */
    public <T> ResponseEntity<T> withBearerHeader(T body, String accessToken) {
        return ResponseEntity.ok()
                .header("Authorization", "Bearer " + accessToken)
                .body(body);
    }
}
