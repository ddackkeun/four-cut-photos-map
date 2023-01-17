package com.idea5.four_cut_photos_map.security.jwt;

import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Collection;
import java.util.Date;

/**
 * JWT 토큰 생성, 검증 관여
 * @See<a href="https://brunch.co.kr/@jinyoungchoi95/1">jwt</>
 * @See<a href="https://annajin.tistory.com/217">jwt 토큰 검증 예외</>
 * @See<a href="https://yeon-blog.tistory.com/3">Claims 객체</>
 * @See<a href="https://velog.io/@jkijki12/Jwt-Refresh-Token-%EC%A0%81%EC%9A%A9%EA%B8%B0">refresh token 발급</>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final SecretKey jwtSecretKey;   // 비밀키
    private long ACCESS_TOKEN_VALIDATION_SECOND = 60 * 60 * 24 * 365 * 100L;    // accessToken 유효기간(100년)

    private SecretKey getSecretKey() {
        return jwtSecretKey;
    }

    /**
     * JWT Access Token 발급
     * @param memberId 회원 id
     * @param authorities 회원 Authority 리스트
     * @return jwt access token
     */
    public String generateAccessToken(Long memberId, Collection<? extends GrantedAuthority> authorities) {
        log.info("accessToken 발급");
        Date now = new Date();
        Claims claims = Jwts.claims()
                .setIssuer("four_cut_photos_map")   // 토큰 발급자
                .setIssuedAt(now)   // 토큰 발급 시간
                .setExpiration(new Date(now.getTime() + 1000L * ACCESS_TOKEN_VALIDATION_SECOND));   // 토큰 만료 시간
        // 회원 기반 정보
        claims.put("id", memberId);
        claims.put("authorities", authorities);

        return Jwts.builder()
                .setClaims(claims)  // Custom Claims 정보(맨 위에 적지않으면 아래 값이 덮어씌워져 누락됨!)
                .signWith(getSecretKey(), SignatureAlgorithm.HS512) // HS512, 비밀키로 서명
                .compact(); // 토큰 생성
    }

    // JWT Access Token 검증
    public boolean verify(String accessToken) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())  // 비밀키
                    .build()
                    .parseClaimsJws(accessToken);   // 파싱 및 검증(실패시 에러)
            return true;
        } catch (SignatureException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (MalformedJwtException e) {
            log.info("유효하지 않은 구성의 JWT 토큰입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 형식이나 구성의 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // accessToken 으로부터 Claim 정보 얻기
    private Claims parseClaims(String accessToken) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(accessToken)
                .getBody();
        log.info(claims.toString());
        return claims;
    }

    // Claims 에서 id
    public Long getId(String accessToken) {
        Claims claims = parseClaims(accessToken);
        // java.lang.Integer cannot be cast to java.lang.Long 오류해결
        return ((Number) claims.get("id")).longValue();
    }
}
